package com.vmware.studio.vamimods.contentresolver.handlers

import com.vmware.studio.shared.mixins.ResourceEnabled
import com.vmware.studio.shared.services.messaging.BaseMessageHandler
import com.vmware.studio.shared.services.messaging.MessageValidator
import groovy.util.logging.Log

/**
 * Created by samueldoyle on 2/21/14.
 * This will handle all requests on the global TOPIC, as well as P2P for the ContentResolverService
 * It itself will register on the Global Channel
 */
@Log(value = "LOGGER")
@Mixin(ResourceEnabled)
class ResourceRequestHandler extends BaseMessageHandler {
    private final ME = this.class.name
    public final String MY_TYPE = "GlobalContentResolver"

    // Services need to provide some basic information
    // webroot
    public final Set SVC_REQUIRED_KEYS = ["svcName", "webRootDir"].asImmutable()
    private final Map REGISTERED_SERVICES_MAP = [:]

    ResourceRequestHandler(String myType) {
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

        REGISTERED_SERVICES_MAP[message.svcName] = message
        OK_RESPONSE()
    }

    def testAddNewService(Map message) {
        LOGGER.info "Performing simulated addNewService operation!"
        OK_RESPONSE(rebootCommand)
    }

    def getServices(Map message) {
        OK_RESPONSE(REGISTERED_SERVICES_MAP)
    }

/***** Implementations Below *****/

    @Override
    Map handle(Map message) {
        // Use method reference directly to avoid any getter/setter interception nastiness
        if (!this.metaClass.respondsTo(this, message.operation as String)) {
            return RESOURCE_ERROR_RESPONSE("services.contentResolverService.errorMessages.unknownOperationType")
        }
        this.&"${message.operation}"(message)
    }
}
