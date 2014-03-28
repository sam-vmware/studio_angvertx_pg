/**
 * Created by samueldoyle
 */
package com.vmware.studio.vertxmods.webserver

import com.jetdrone.vertx.yoke.GYoke
import com.jetdrone.vertx.yoke.engine.GroovyTemplateEngine
import com.jetdrone.vertx.yoke.middleware.*
import com.vmware.studio.shared.categories.CommonService
import com.vmware.studio.shared.mixins.ContextConfig
import com.vmware.studio.shared.mixins.ResourceEnabled
import com.vmware.studio.shared.services.Service
import com.vmware.studio.shared.utils.ClosureScriptAsClass
import com.vmware.studio.shared.utils.GlobalServiceConfig
import com.vmware.studio.shared.utils.SessionUtils
import org.jvnet.libpam.PAM
import org.jvnet.libpam.PAMException
import org.jvnet.libpam.UnixUser
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.json.JsonObject
import org.vertx.java.core.sockjs.EventBusBridgeHook

@Mixin([ResourceEnabled, ContextConfig, CommonService])
class WebServiceLoader extends Verticle implements Service {
    final String CURRENT_ENVIRONMENT = System.properties["env"] ?: "dev"

    def authManagerConf = [
        // addres: 'vertx.basicauthmanager',
        // persistor_address: 'vertx.mongopersistor',
        user_collection: 'users',
        session_timeout: 1800000 // 30mins
    ]

    def mongoPersistorConf = [
        host   : 'localhost',
        port   : 27017,
        db_name: 'studio_db'
    ]

    def webServerConf = [
        //host              : 'localhost',
        host              : '0.0.0.0',
        port              : 8080,
        web_root          : '', // fill this in or defaults to the mod directory (which it should probably be)
        // web_root          : '/home/samueldoyle/Projects/VMware/StudioRepo/git/NewStack/studio_angular_vertx/web-ui/app',
        //web_root          : '/opt/vmware/share/vertx_extra/web_root/web_app',
        index_page        : 'index.html',
        static_files      : true,
        ssl               : false,
        bridge            : true,
        inbound_permitted : [[:]],
        outbound_permitted: [[:]]
    ]

    def DEFAULT_PORT = 8080;
    def DEFAULT_ADDRESS = "0.0.0.0";
    def DEFAULT_WEB_ROOT = "web";
    def DEFAULT_INDEX_PAGE = "index.html";
//    def DEFAULT_AUTH_ADDRESS = "vertx.basicauthmanager.authorise";
    def DEFAULT_AUTH_TIMEOUT = 5 * 60 * 1000;

    def myConfigObject = {
        services {
            newServiceAnnounceChannel = "vami.newServiceAnnounceChannel"
            webService {
                // This is the directory we expect to find be available from the start directory
                // which should be the VERTX_MODS directory e.g.
                // MY_ROOT_SHOULD_BE_HERE = $VERTX_MODS/$expectedRootMatch
                expectedRootMatch = "com.vmware~vami-web-server~1.0"
            }
        }
        environments {
            dev {
            }
            test {
            }
            prod {
            }
        }
    }

    def postInitialize() {
    }

    /*********************************** OVERRIDES BELOW ***********************************/

    def routeMatcher = new RouteMatcher()

    @Override
    def start() {
        container.logger.info "Deployment succeeded"
        container.logger.info "*** Environment: $CURRENT_ENVIRONMENT"

        // Load config
        loadLocalResource(new ClosureScriptAsClass(myConfigObject))

        // Share container and vertx
        SET_CONTAINER(container).SET_VERTX(vertx)

        DUMP_CONTAINER_CONF()
        DUMP_CONTAINER_ENV()
        // CommonService Mixin on Service
        // Verify our install root is where it should be
        // TBD embed this in some config logic
        if (CURRENT_ENVIRONMENT != "test") {
            VERIFY_INSTALL_ROOT()
        }

        postInitialize()

        // Serve the static resources
        def host = webServerConf["host"]
        def port = webServerConf["port"]
        def baseRoot = webServerConf["web_root"] ?: GET_MOD_DIR()
        def myWebRoot = "${getExpectedRoot().name}/app"
        container.logger.with {
            info "========== WebServiceLoader Starting =========="
            info "host: $host"
            info "port: $port"
            info "base root: $baseRoot"
            info "relative root: $myWebRoot"
            info "==============================================="
        }

        def secret = SessionUtils.instance.generateNewSecret()
        def router = new GRouter()
            .post("/auth/login") { request ->
            def body = request.formAttributes
            String userName = body[SessionUtils.instance.httpValues.nameField]
            String passWord = body[SessionUtils.instance.httpValues.pwdField]
            try {
                if (userName && passWord) {
                    UnixUser unixUser = new PAM("sshd").authenticate(userName, passWord);

                    // Create and initialize the new session
                    def nameField = SessionUtils.instance.httpValues.nameField
                    def bridgeAuthField = SessionUtils.instance.eventBusValues.bridgeAuthField
                    def session = SessionUtils.instance.createAndInitGYokeSession(request, new JsonObject([(nameField): body[nameField]]))
                    def newSessionID = session.getString(bridgeAuthField)
                    def sessionIDCooke = SessionUtils.instance.createSessionIDCookie(newSessionID)

                    // Set the sessionID cookie so we can use it in eventbus requests
                    request.response.addCookie(sessionIDCooke)
                    request.response.end OK_RESPONSE()

                    return
                }
            } catch (PAMException pamException) {
                request.response.end ERROR_RESPONSE([
                    error: [
                        message: "Authentication Failed: ${pamException.message}"
                    ]
                ])
            }
        }
        .get("/auth/logout") { request ->
            request.destroySession()
            request.response().redirect("/");
        }

        def secHandler = { request, next ->
            if (!(request.uri ==~ /^\/com\..+/)) next.handle(null)

            def session = request.getString(SessionUtils.instance.httpValues.requestSessionField);
            def uname = session?.getString(SessionUtils.instance.httpValues.nameField)
            if (!uname) {
                next.handle(401)
                return
            }
            next.handle(null)
        }

        def getSessionValue = { request, key ->
            def session = request.getString(SessionUtils.instance.httpValues.requestSessionField);
            session?.getString(key)
        }

        def server = vertx.createHttpServer()
        new GYoke(this)
            .engine('html', new GroovyTemplateEngine())
            .use(new ErrorHandler(true))
            .use(new CookieParser(secret))
            .use(new Session(secret))
            .use(new Logger())
//            .use(new BridgeSecureHandler(SessionUtils.instance.eventBusValues.authAddress, sharedDataSessionStore))
//            .use("/static", new Static("."))
//            .use("/modules", secHandler)
            .use(new BodyParser())
            .use(router)
            .use { request, next ->
            def file
            def secure = false
            switch (request.uri) {
                case "/":
                case "/?":
                    file = "$baseRoot/$myWebRoot/index.html"
                    if (SessionUtils.instance.isAuthenticated(request)) {
                        file = "$baseRoot/$myWebRoot/modules/main/views/mainApp.html"
                    }
                    break
                case ~/^\/com\..+/:
                    file = "$baseRoot/${request.uri}"
                    secure = true
                    break
                case "/modules":
                    secure = true
                default:
                    file = "$baseRoot/$myWebRoot/${request.uri}"
            }
            if (secure) {
                if (!SessionUtils.instance.isAuthenticated(request)) {
                    //next.handle(401)
                    //return
                    request.response().redirect("/");
                    return
                }
            }
            request.response.sendFile file
        }.listen(server)

/*        server.requestHandler { req ->
            def file
            switch (req.uri) {
                case "/":
                    file = "$baseRoot/$myWebRoot/index.html"
                    break
                case ~/^\/com\..+/:
                    file = "$baseRoot/${req.uri}"
                    break
                default:
                    file = "$baseRoot/$myWebRoot/${req.uri}"
            }
            req.response.sendFile file
        }*/

        def config = [prefix: "/eventbus"]
        def inboundPermitted = [[
            address_re   : ".*",
            requires_auth: true
        ]]
        def outboundPermitted = [[:]]
        def authTimeout = SessionUtils.instance.eventBusValues.authTimeout
        def authAddress = SessionUtils.instance.eventBusValues.authAddress

        SessionUtils.instance.startSecureBridgeHandler(vertx)

        vertx.createSockJSServer(server)
            .bridge(config, inboundPermitted, outboundPermitted, authTimeout, authAddress)
            .setHook(SessionUtils.instance.createEventBridgeHook())

        server.listen(port, host)
    }

    /*********************************** OVERRIDES BELOW ***********************************/

    @Override
    File getExpectedRoot() {
        new File(GET_CONFIG().services.webService.expectedRootMatch)
    }

    @Override
    void registerService() {
        // webserviceloader has no content
    }

    @Override
    void handleGlobalResourceMessage(Map message) {
        // webserviceloader has no content
    }

    @Override
    String getServiceName() {
        GlobalServiceConfig.instance.webServiceCommonConfig.service.name
    }

    @Override
    String getAddress() {
        GlobalServiceConfig.instance.webServiceCommonConfig.service.address
    }
}
