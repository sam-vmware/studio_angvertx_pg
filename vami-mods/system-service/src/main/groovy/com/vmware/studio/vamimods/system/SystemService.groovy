package com.vmware.studio.vamimods.system

import com.vmware.studio.services.messaging.BaseMessageHandler
import com.vmware.studio.services.messaging.MessageValidator
import com.vmware.studio.utils.ClosureScriptAsClass
import com.vmware.studio.utils.ResourceLoader
import org.vertx.groovy.platform.Verticle
import org.vertx.groovy.core.eventbus.Message
import com.vmware.studio.vamimods.system.helpers.*
import org.vertx.java.core.json.JsonObject

/**
 * Simple verticle to represent system service
 * Messages should be of similar type to CIM, get,set,create ...
 * def msg = [
 * type: ...
 * operation: ...
 * data: ...
 * ]
 */
class SystemService extends Verticle {
    public final static String MY_ADDRESS = SystemService.class.name
    def HANDLER_REGISTRY = []
    // private static String myConfigObject = "com.vmware.studio.vamimods.system.resources.SystemService"

    // Until I can figure out how to deal with the Vert.X classloader stuff
    // Belongs in resources/SystemServiceConfig
    def myConfigObject = {
        services {
            systemService {
                bootStrapVerticle = "groovy:com.vmware.studio.vamimods.system.SystemServiceBootstrap"
                errorMessages {
                    unknownMessageType = "Unknown message type received"
                    unknownOperationType = "Unknown operation type received"
                    msgValidationFailure = "Message validation failed"
                    invalidMessagePayload = "Invalid message body payload type received"
                }
                environments {
                    dev {
                    }
                }
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
        def tzHandler = new TimeZoneMessageHandler()
        def HANDLER_REGISTRY = [
                "${tzHandler.type}": tzHandler
        ]

        // Register with the event bus
        def eb = vertx.eventBus
        eb.registerLocalHandler(MY_ADDRESS, { Message message ->
            container.logger.debug "Received Message: ${message.body()}"
            if (!message.body() instanceof JsonObject) {
                message.reply(BaseMessageHandler.ERROR_RESPONSE(
                        ResourceLoader.instance.getConfigProperty("services.systemService.errorMessages.invalidMessagePayload")
                ))
            }
            message.reply(handleMessage(message.body()))
        })
    }

    /**
     * Default handler
     * @param message - all Groovy eventbus messages are maps
     * @return - response or error
     */
    def handleMessage(message) {
        def handler = HANDLER_REGISTRY[message.body.type]
        if (!handler) {
            return ERROR_RESPONSE(
                    "${ResourceLoader.instance.getConfigProperty("services.systemService.errorMessages.unknownMessageType")}: $invalid"
            )
        }

        // Basic failfast validation
        def validInfo = MessageValidator.validate(message)
        def invalid = validInfo.invalid
        if (invalid) {
            return ERROR_RESPONSE(
                    "${ResourceLoader.instance.getProperty("services.systemService.errorMessages.msgValidationFailure")}: $invalid"
            )
        }

        handler.handle(message)
    }

    /**
     * Vertx deployer entry
     * @return
     */
    def start() {
        container.logger.info "Deployment succeeded for: ${this.class.name}"

        // Load config
        ResourceLoader.instance.loadConfigObject(new ClosureScriptAsClass(closure: myConfigObject))
        def bootStrap = ResourceLoader.instance.getConfigProperty("services.systemService.bootStrapVerticle")

        if (bootStrap) {
            container.logger.debug "Found bootStrap: $bootStrap"
            container.deployVerticle(bootStrap) { asyncResult ->
                if (asyncResult.succeeded) {
                    postInitialize()
                } else {
                    container.logger.error "Failed to deploy ${asyncResult.result()}"
                }
            }
        } else {
            container.logger.debug "No bootStrap found"
            postInitialize()
        }
    }
}