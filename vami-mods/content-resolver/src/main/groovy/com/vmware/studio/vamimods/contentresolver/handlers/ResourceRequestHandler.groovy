package com.vmware.studio.vamimods.contentresolver.handlers

import com.vmware.studio.shared.mixins.ContextConfig
import com.vmware.studio.shared.mixins.ResourceEnabled
import com.vmware.studio.shared.services.messaging.BaseMessageHandler
import com.vmware.studio.shared.services.messaging.MessageValidator
import com.vmware.studio.shared.utils.GlobalServiceConfig
import groovy.util.logging.Log
import org.vertx.java.core.json.JsonObject

/**
 * Created by samueldoyle on 2/21/14.
 * This will handle all requests on the global TOPIC, as well as P2P for the ContentResolverService
 * It itself will register on the Global Channel
 */
@Log(value = "LOGGER")
@Mixin([ResourceEnabled, ContextConfig])
class ResourceRequestHandler extends BaseMessageHandler {
    public static final String MY_TYPE = "GlobalContentResolver"

    // Services need to provide some basic information
    // webroot
    public final Set SVC_REQUIRED_KEYS = ["svcName", "webRootDir", "indexFile"].asImmutable()

    private final JsonObject REGISTERED_SERVICES_MAP = new JsonObject()

    public ResourceRequestHandler(String myType = MY_TYPE) {
        super(myType)
    }

    /**
     * A P2P message to register a new service
     * @param message
     * @return
     */
    def addNewService(Map message) {

        def data = message.data
        def validInfo = MessageValidator.instance.validate(data, SVC_REQUIRED_KEYS)
        def valid = validInfo.valid

        if (!valid) {
            return RESOURCE_ERROR_RESPONSE("services.contentResolverService.errorMessages.regSvcMsgValidationFailure",
                SVC_REQUIRED_KEYS as String)
        }

        String svcName = message.data.svcName
        if (!svcName) {
            return ERROR_RESPONSE("addNewService: svcName was not set")
        }
        REGISTERED_SERVICES_MAP.removeField(svcName)
        REGISTERED_SERVICES_MAP.putValue(svcName, new JsonObject(message))

        def newServiceAnnounceChannel = GlobalServiceConfig.instance.globalServiceCommonConfig.service.newServiceAnnounceChannel

        // Announce new service to listeners
        LOGGER.info "Publishing new service registration announce: channel -> $newServiceAnnounceChannel, data -> $message"
        GET_VERTX().eventBus.publish(newServiceAnnounceChannel, REGISTERED_SERVICES_MAP.getValue(svcName));

        OK_RESPONSE()
    }

    def testAddNewService(Map message) {

        def data = message.data
        def validInfo = MessageValidator.instance.validate(data, SVC_REQUIRED_KEYS)
        def valid = validInfo.valid

        if (!valid) {
            return RESOURCE_ERROR_RESPONSE("services.contentResolverService.errorMessages.regSvcMsgValidationFailure",
                SVC_REQUIRED_KEYS as String)
        }

        String svcName = message.data.svcName
        assert svcName != null
        REGISTERED_SERVICES_MAP.removeField(svcName)
        REGISTERED_SERVICES_MAP.putValue(svcName, new JsonObject(message))

        OK_RESPONSE()
    }

    def getServices(Map message) {
        def returnResults = REGISTERED_SERVICES_MAP.toMap().inject([:]) { newMap, k, v ->
            newMap << [(k):v]
            newMap
        }
        OK_RESPONSE(returnResults)
    }

    def testGetServices(Map message) {
        def response = [
            [key: "testSvc", value: [
                svcName: "testService", webRootDir: "/tmp", indexFile: "index.html"
            ]],
        ]
        OK_RESPONSE(response)
    }

    /*********************************** OVERRIDES BELOW ***********************************/

    @Override
    Map handle(Map message) {
        // Use method reference directly to avoid any getter/setter interception nastiness
        if (!this.metaClass.respondsTo(this, message.operation as String)) {
            return RESOURCE_ERROR_RESPONSE("services.contentResolverService.errorMessages.unknownOperationType")
        }
        def operation = message.operation
        message = stripPostProcessKeys(message)
        this.&"$operation"(message)
    }
}
