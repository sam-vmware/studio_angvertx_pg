package com.vmware.studio.shared.utils

import com.fasterxml.uuid.EthernetAddress
import com.fasterxml.uuid.Generators
import com.fasterxml.uuid.impl.TimeBasedGenerator
import com.jetdrone.vertx.yoke.middleware.GYokeRequest
import com.jetdrone.vertx.yoke.middleware.YokeCookie
import com.jetdrone.vertx.yoke.store.GSharedDataSessionStore
import com.jetdrone.vertx.yoke.util.Utils
import groovy.transform.WithReadLock
import groovy.transform.WithWriteLock
import groovy.util.logging.Log
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.eventbus.Message
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.Handler
import org.vertx.java.core.json.JsonObject
import org.vertx.java.core.sockjs.EventBusBridgeHook
import org.vertx.java.core.sockjs.SockJSSocket

import javax.crypto.Mac

/**
 * Created by samueldoyle on 3/18/14.
 * Place to put session related common functionality
 */
@Log(value = "LOGGER")
@Singleton
class SessionUtils {
    public final sessionStoreSuffix = "session.store"
    public final httpValues = [
        nameField   : "username",
        pwdField    : "password",
        authAddress : "auth.address",
        requestSessionField: "session"
    ].asImmutable()

    public final eventBusValues = [
        bridgeAuthField: "sessionID", // Checked by BridgeSecureHandler against the SessionStore
        authAddress : "auth.address",
        authTimeout: 5 * 60 * 1000
    ].asImmutable()

    private final sessionCookieFields = [
        path: "/",
        secure : false,
        maxAge: 60 * 60 * 1000
    ].asImmutable()

    public UUID genV1UUID() {
        EthernetAddress addr = EthernetAddress.fromInterface()
        TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(addr)
        uuidGenerator.generate()
    }

    final Map STATUS_OK = [status: "ok"].asImmutable()
    final Map STATUS_DENIED = [status: "denied"].asImmutable()
    def mySessionStore

    public String createAndSetSessionID(JsonObject session) {
        def newSessionID = genV1UUID().toString()
        LOGGER.info "Generated new Session ID with id: $newSessionID"
        session.putString(eventBusValues.bridgeAuthField, newSessionID)
        newSessionID
    }

    @WithReadLock
    private GSharedDataSessionStore getTheSessionStore() {
        mySessionStore
    }

    @WithWriteLock
    private void initTheSessionStore(Vertx vertx) {
        if (!mySessionStore) {
            LOGGER.info "Initializing Session Store"
            mySessionStore = createSessionStore(vertx)
        } else {
            LOGGER.info "Session Store already initialized"
        }
    }

    public GSharedDataSessionStore createSessionStore(Vertx vertx) {
        def sessionStoreName = "${genV1UUID().toString()}_$sessionStoreSuffix"
        LOGGER.info "Generated new Session Store with name: $sessionStoreName"
        new GSharedDataSessionStore(vertx.toJavaVertx(), sessionStoreName)
    }

    public Mac generateNewSecret() {
        def secret = genV1UUID().toString()
        Mac mac = Utils.newHmacSHA256(secret)
        LOGGER.info "Generated new MAC with Secret: $secret"
        mac
    }

    /**
     * Create a new session from a request, likely done from successful login
     * @param request
     * @param additionalSessionData - any additional session attributes to set
     * @return
     */
    public JsonObject createAndInitGYokeSession(GYokeRequest request, JsonObject additionalSessionData = new JsonObject()) {
        JsonObject newSession = request.createSession()
        newSession.mergeIn(additionalSessionData)
        def sessionID = createAndSetSessionID(newSession)
        getTheSessionStore().set(sessionID, newSession, {
            LOGGER.info "Successfully set session for sessionID: $sessionID"
        })
        newSession
    }

    /**
     * Simply check if the sessionID has been set, change this later
     * @param request
     * @return
     */
    public boolean isAuthenticated(GYokeRequest request) {
        JsonObject session = request.get(httpValues.requestSessionField) as JsonObject
        session?.getString(eventBusValues.bridgeAuthField) != null
    }

    /**
     * In order to secure the eventbus need to link the session the id from our successful login to
     * that of the sessionID in each EB message, this sets a cookie on the response that the client
     * will then need to use when sending EB messages
     * @param sessionID
     * @return
     */
    public YokeCookie createSessionIDCookie(String sessionID) {
        def sessionIDCookie = new YokeCookie(eventBusValues.bridgeAuthField, generateNewSecret())
        sessionIDCookie.path = sessionCookieFields.path
        sessionIDCookie.secure = sessionCookieFields.secure
        sessionIDCookie.maxAge = sessionCookieFields.maxAge
        sessionIDCookie.value = sessionID

        sessionIDCookie
    }

    /**
     * This is the auth handler, each message on the bridge will go through this and all it
     * does atm is check to see if the sessionid is present in session store
     * @param vertx
     */
    public void startSecureBridgeHandler(Vertx vertx) {
        LOGGER.info "Registering Secure Bridge Listener @ ${eventBusValues.authAddress}"
        initTheSessionStore(vertx)
        vertx.eventBus.registerHandler(eventBusValues.authAddress, { Message message ->
            def msgBody = message.body()
            String sessionID = msgBody[eventBusValues.bridgeAuthField]
            if (!sessionID) {
                message.reply(STATUS_DENIED)
                return
            }
            getTheSessionStore().get(sessionID, { Map session ->
               if (!session) {
                   LOGGER.info "Session for sessionID: $sessionID was null"
                   message.reply(STATUS_DENIED)
                   return
               }
               LOGGER.info "Session for sessionID: $sessionID was GOOD!"
               def response = STATUS_OK + [(httpValues.nameField):session[httpValues.nameField]]
               message.reply(response)
            });
        })
    }

    /**
     * NOOP ATM
     * @return
     */
    public EventBusBridgeHook createEventBridgeHook() {
        return new EventBusBridgeHook() {
            @Override
            boolean handleSocketCreated(SockJSSocket sock) {
                LOGGER.info "EventBridgeHook.handleSocketCreated"
                true
            }

            @Override
            void handleSocketClosed(SockJSSocket sock) {
                LOGGER.info "EventBridgeHook.handleSocketClosed"
            }

            @Override
            boolean handleSendOrPub(SockJSSocket sock, boolean send, JsonObject msg, String address) {
                LOGGER.info "EventBridgeHook.handleSendOrPub"
                true
            }

            @Override
            boolean handlePreRegister(SockJSSocket sock, String address) {
                LOGGER.info "EventBridgeHook.handlePreRegister"
                true
            }

            @Override
            void handlePostRegister(SockJSSocket sock, String address) {
                LOGGER.info "EventBridgeHook.handlePostRegister"
            }

            @Override
            boolean handleUnregister(SockJSSocket sock, String address) {
                LOGGER.info "EventBridgeHook.handleUnregister"
                true
            }

            @Override
            boolean handleAuthorise(JsonObject message, String sessionID, Handler<AsyncResult<Boolean>> handler) {
                LOGGER.info "EventBridgeHook.handleAuthorise"
                true
            }
        }
    }
}
