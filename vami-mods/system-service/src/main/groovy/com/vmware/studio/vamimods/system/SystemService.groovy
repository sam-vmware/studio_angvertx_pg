/**
 * Created by samueldoyle
 */
package com.vmware.studio.vamimods.system

import com.vmware.studio.shared.categories.CommonService
import com.vmware.studio.shared.mixins.ContextConfig
import com.vmware.studio.shared.mixins.MessageHandlerRegistry
import com.vmware.studio.shared.mixins.ResourceEnabled
import com.vmware.studio.shared.services.Service
import com.vmware.studio.shared.services.messaging.MessageValidator
import com.vmware.studio.shared.services.messaging.types.GlobalMsgTypes
import com.vmware.studio.shared.utils.ClosureScriptAsClass
import com.vmware.studio.shared.utils.GlobalServiceConfig
import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.platform.Verticle

/**
 * Simple verticle to represent system service
 * Messages should be of similar type to CIM, get,set,create ...
 * def msg = [
 * type: ...
 * operation: ...
 * data: ...
 * ]
 */
@Mixin([ResourceEnabled, MessageHandlerRegistry, ContextConfig, CommonService])
class SystemService extends Verticle implements Service {
    final String CURRENT_ENVIRONMENT = System.properties["env"] ?: "dev"

    // Until I can figure out how to deal with the Vert.X classloader stuff
    // Belongs in resources/SystemServiceConfig
    def myConfigObject = {
        services {
            systemService {
                bootStrapVerticle = "groovy:com.vmware.studio.vamimods.system.SystemServiceBootstrap"
                handlers = [
                    [name: "TimeZone", FQCN: "com.vmware.studio.vamimods.system.helpers.TimeZoneMessageHandler", enabled: true],
                    [name: "SystemInformation", FQCN: "com.vmware.studio.vamimods.system.helpers.InformationMessageHandler", enabled: true],
                    [name: "OperatingSystem", FQCN: "com.vmware.studio.vamimods.system.helpers.OperatingSystemHelper", enabled: true]
                ]
                // This is the directory we expect to find be available from the start directory
                // which should be the VERTX_MODS directory e.g.
                // MY_ROOT_SHOULD_BE_HERE = $VERTX_MODS/$expectedRootMatch
                expectedRootMatch = "com.vmware~vami-system-service~1.0"
                errorMessages {
                    unknownMessageType = "Unknown message type received"
                    unknownOperationType = "Unknown operation type received"
                    msgValidationFailure = "Message validation failed"
                    invalidMessagePayload = "Invalid message body payload type received"
                    invalidNewTimezone = "Missing or invalid new timezone value"
                    missingZoneFile = "The specified timezone file doesn't exist"
                    unknownOS = "Unable to determine Operating System"
                    failedToUpdateTZ = "Unable to determine Operating System"
                    manifestFileNotReadable = "The provided manifest file is not accessible : "
                    rebootCmdFailed = "Failed to perform reboot operation : "
                    shutdownCmdFailed = "Failed to perform shutdown operation : "
                }
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

    /**
     * Load resources
     * Register message handlers
     * ...
     */
    def postInitialize() {

        // Register Handlers
        def handlers = GET_CONFIG().services.systemService.handlers
        def instance
        for (svc in handlers) {
            if (svc.enabled) {
                container.logger.info "Registering enabled service: ${svc.name}"
                instance = this.class.classLoader.loadClass(svc.FQCN, true)?.newInstance(svc.name)
                addHandler(instance)
            } else {
                container.logger.info "Skpping disabled service ${svc.name} for registry"
            }
        }

        // Register with the event bus for this service
        container.logger.info "Registering local service address handler @ ${getAddress()}"
        vertx.eventBus.registerHandler(getAddress(), { Message message ->
            container.logger.info "Received Message: ${message.body()}"
            def msgBody = message.body()
            if (!(msgBody instanceof Map)) {
                message.reply(RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.invalidMessagePayload"))
            } else {
                def replyMessage = handleMessage(message.body())
                container.logger.info "Sending Response Message: $replyMessage"
                message.reply(replyMessage)
            }
        })

        registerService()

        // Register with the global resource topic -- CommonService provided
        GLOBAL_RESOURCE_SUBSCRIBE()

        this
    }

    /**
     * Default handler
     * @param message - all Groovy eventbus messages are maps
     * @return - response or error
     */
    def handleMessage(message, handlerType = message.type) {
        // Basic failfast validation
        def validInfo = MessageValidator.instance.validate(message)
        def valid = validInfo.valid

        if (!valid) {
            return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.msgValidationFailure")
        }

        def handler = lookupHandler(handlerType as String)
        if (!handler) {
            return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.unknownMessageType")
        }

        handler.handle(message)
    }

    /*********************************** OVERRIDES BELOW ***********************************/

    /**
     * Vertx deployer entry
     * @return
     */
    @Override
    def start() {
        container.logger.info "Deployment succeeded for"
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
    }

    /**
     * Register the new service with the global resource handling service
     */
    @Override
    void registerService() {
        // Register with the content service
        def globalResourceAddress = GlobalServiceConfig.instance.contentResolverServiceCommonConfig.service.address
        container.logger.info "Registering with content resolver service: $globalResourceAddress"

        def myWebRootDir = "${getExpectedRoot().name}/app"

        def resourceSvcRegistrationMsg =
            GlobalServiceConfig.instance.systemServiceCommonConfig.service.web.messages.resourceSvcRegistrationMsg
        resourceSvcRegistrationMsg.data.webRootDir = myWebRootDir

        if (CURRENT_ENVIRONMENT != "test") {
            container.logger.info "Sending new service registration message to: $globalResourceAddress, data: $resourceSvcRegistrationMsg"
            vertx.eventBus.send(globalResourceAddress as String, resourceSvcRegistrationMsg, { reply ->
                container.logger.info "Received response from content service: ${reply.body}"
            })
        }

        this
    }

    @Override
    File getExpectedRoot() {
        new File(GET_CONFIG().services.systemService.expectedRootMatch)
    }

    @Override
    void handleGlobalResourceMessage(Map message) {
        def msgType = message.type as GlobalMsgTypes
        switch (msgType) {
            case GlobalMsgTypes.REREGISTER_SERVICE:
                container.logger.info "Received service reregister request from originator: ${message.originatorAddress}, sending now."
                registerService()
                break
            default:
                ERROR_RESPONSE("Unknown Global Resource Message Type", msgType)
        }
    }

    @Override
    String getServiceName() {
        GlobalServiceConfig.instance.systemServiceCommonConfig.service.name
    }

    @Override
    String getAddress() {
        GlobalServiceConfig.instance.systemServiceCommonConfig.service.address
    }
}