
package com.vmware.studio.vamimods.contentresolver

import com.vmware.studio.shared.mixins.MessageHandlerRegistry
import com.vmware.studio.shared.mixins.ResourceEnabled
import com.vmware.studio.shared.services.Service
import com.vmware.studio.shared.services.messaging.MessageValidator
import com.vmware.studio.shared.utils.ClosureScriptAsClass
import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.platform.Verticle

/**
* Created by samueldoyle
* Resource specific handling entry point
*/
@Mixin([ResourceEnabled, MessageHandlerRegistry])
class ContentResolverService extends Verticle implements Service {
    private final ME = this.class.name
    public final static String MY_ADDRESS = "vami.ContentResolverService"

    def myConfigObject = {
        services {
            contentResolverService {
                handlers = [
                    [name: "ResourceRequestHandler", FQCN: "com.vmware.studio.vamimods.contentresolver.handlers.ResourceRequestHandler", enabled: true]
                ]
                GLOBAL_RESOURCE_CHANNEL_ID = "GLOBAL_RESOURCE_CHANNEL"
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
        container.logger.info "Registering local service address handler @ $MY_ADDRESS"
        vertx.eventBus.registerHandler(MY_ADDRESS, { Message message ->
            container.logger.info "$ME Received Message: ${message.body()}"
            def msgBody = message.body()
            if (!(msgBody instanceof Map)) {
                message.reply(RESOURCE_ERROR_RESPONSE("services.contentResolverService.errorMessages.invalidMessagePayload"))
            } else {
                def replyMessage = handleMessage(message.body())
                container.logger.info "$ME Sending Response Message: $replyMessage"
                message.reply(replyMessage)
            }
        })

        // Also we register on the global resource topic
        def globalResourceAddress = LOOKUP_CONFIG("services.contentResolverService.GLOBAL_RESOURCE_CHANNEL_ID")
        vertx.eventBus.registerHandler(globalResourceAddress as String, { Message message ->
            container.logger.info "$ME Received Global Resource Message: ${message.body()}"
            def msgBody = message.body()
            if (!(msgBody instanceof Map)) {
                message.reply(RESOURCE_ERROR_RESPONSE("services.contentResolverService.errorMessages.invalidMessagePayload"))
            } else {
                def replyMessage = handleMessage(message.body(), "ResourceRequestHandler")
                container.logger.info "$ME Sending Response Message: $replyMessage"
                message.reply(replyMessage)
            }
        })
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

    /**
     * Vertx deployer entry
     * @return
     */
    def start() {
        container.logger.info "$ME Deployment succeeded for: ${this.class.name}"

        // Load config
        loadLocalResource(new ClosureScriptAsClass(closure: myConfigObject))
        postInitialize()
    }

}
