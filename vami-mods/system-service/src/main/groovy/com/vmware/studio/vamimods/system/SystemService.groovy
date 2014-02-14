package com.vmware.studio.vamimods.system

import com.vmware.studio.services.messaging.BaseMessageHandler
import com.vmware.studio.services.messaging.MessageValidator
import com.vmware.studio.utils.ClosureScriptAsClass
import com.vmware.studio.utils.ResourceLoader
import org.vertx.groovy.platform.Verticle
import org.vertx.groovy.core.eventbus.Message
import com.vmware.studio.vamimods.system.helpers.*

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
    private Map HANDLER_REGISTRY = [:]
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
        for (svc in [new TimeZoneMessageHandler()]) {
            HANDLER_REGISTRY += [(svc.type): svc]
        }

        // Register with the event bus
        vertx.eventBus.registerLocalHandler(MY_ADDRESS, { Message message ->
            container.logger.info "Received Message: ${message.body()}"
            container.logger.info "Body Type: ${message.body().class}"
            if (!(message.body() instanceof Map)) {
                message.reply(BaseMessageHandler.ERROR_RESPONSE(
                        ResourceLoader.instance.getConfigProperty("services.systemService.errorMessages.invalidMessagePayload")
                ))
            } else {
                message.reply(handleMessage(message.body()))
            }
        })
    }

    /**
     * Default handler
     * @param message - all Groovy eventbus messages are maps
     * @return - response or error
     */
    def handleMessage(message) {
        // Basic failfast validation
        def validInfo = MessageValidator.instance.validate(message)
        def invalid = validInfo.invalid
        if (invalid) {
            return BaseMessageHandler.ERROR_RESPONSE(
                    "${ResourceLoader.instance.getConfigProperty("services.systemService.errorMessages.msgValidationFailure")}: $invalid"
            )
        }

        def handler = HANDLER_REGISTRY[(message.type)]
        if (!handler) {
            return BaseMessageHandler.ERROR_RESPONSE(
                    "${ResourceLoader.instance.getConfigProperty("services.systemService.errorMessages.unknownMessageType")}: $invalid"
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
        postInitialize()

        /*
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
          }*/
    }
}