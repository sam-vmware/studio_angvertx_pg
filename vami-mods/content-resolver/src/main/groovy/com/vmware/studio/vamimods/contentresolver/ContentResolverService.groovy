package com.vmware.studio.vamimods.contentresolver

import com.vmware.studio.shared.categories.CommonService
import com.vmware.studio.shared.mixins.ContextConfig
import com.vmware.studio.shared.mixins.MessageHandlerRegistry
import com.vmware.studio.shared.mixins.ResourceEnabled
import com.vmware.studio.shared.services.Service
import com.vmware.studio.shared.services.messaging.MessageValidator
import com.vmware.studio.shared.utils.ClosureScriptAsClass
import com.vmware.studio.shared.utils.GlobalServiceConfig
import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.platform.Verticle

/**
 * Created by samueldoyle
 * Resource specific handling entry point
 */
@Mixin([ResourceEnabled, MessageHandlerRegistry, ContextConfig, CommonService])
class ContentResolverService extends Verticle implements Service {
    final String CURRENT_ENVIRONMENT = System.properties["env"] ?: "dev"

    def myConfigObject = {
        services {
            contentResolverService {
                handlers = [
                    [name: "ResourceRequestHandler", FQCN: "com.vmware.studio.vamimods.contentresolver.handlers.ResourceRequestHandler", enabled: true]
                ]
                // This is the directory we expect to find be available from the start directory
                // which should be the VERTX_MODS directory e.g.
                // MY_ROOT_SHOULD_BE_HERE = $VERTX_MODS/$expectedRootMatch
                expectedRootMatch = "com.vmware~vami-content-resolver~1.0"
                errorMessages {
                    unknownMessageType = "Unknown message type received"
                    unknownOperationType = "Unknown operation type received"
                    msgValidationFailure = "Message validation failed"
                    invalidMessagePayload = "Invalid message body payload type received"
                    regSvcMsgValidationFailure = "New service validation failed, missing one or more requried keys : "
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
        def handlers = GET_CONFIG().services.contentResolverService.handlers
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

        // Register with the event bus
        container.logger.info "Registering local service address handler @ ${getAddress()}"
        vertx.eventBus.registerHandler(getAddress(), { Message message ->
            container.logger.info "Received Message: ${message.body()}"
            def msgBody = message.body()
            if (!(msgBody instanceof Map)) {
                message.reply(RESOURCE_ERROR_RESPONSE("services.contentResolverService.errorMessages.invalidMessagePayload"))
            } else {
                def replyMessage = handleMessage(message.body())
                container.logger.info "Sending Response Message: ${replyMessage.toString()}"
                message.reply(replyMessage)
            }
        })

        // Send reregister message just in case other services are up already
        def globalChannel = GlobalServiceConfig.instance.globalServiceCommonConfig.service.globalChannel
        def reregMsg = GlobalServiceConfig.instance.globalServiceCommonConfig.service.messages.reregister
        vertx.eventBus.publish(globalChannel as String, reregMsg)
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
            return RESOURCE_ERROR_RESPONSE("services.contentResolverService.errorMessages.msgValidationFailure")
        }

        def handler = lookupHandler(handlerType)
        if (!handler) {
            return RESOURCE_ERROR_RESPONSE("services.contentResolverService.errorMessages.unknownMessageType", handlerType)
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
    }

    @Override
    File getExpectedRoot() {
        new File(GET_CONFIG().services.contentResolverService.expectedRootMatch)
    }

    @Override
    void registerService() {
        // content resolver has no content
    }

    @Override
    void handleGlobalResourceMessage(Map message) {
        // content resolver has no content
    }

    @Override
    String getServiceName() {
        GlobalServiceConfig.instance.contentResolverServiceCommonConfig.service.name
    }

    @Override
    String getAddress() {
        GlobalServiceConfig.instance.contentResolverServiceCommonConfig.service.address
    }
}
