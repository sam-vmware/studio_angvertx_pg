package com.vmware.studio.vamimods.system

import com.vmware.studio.shared.mixins.MessageHandlerRegistry
import com.vmware.studio.shared.mixins.ResourceEnabled
import com.vmware.studio.shared.services.Service
import com.vmware.studio.shared.services.messaging.MessageValidator
import com.vmware.studio.shared.utils.ClosureScriptAsClass
import com.vmware.studio.vamimods.system.helpers.TimeZoneMessageHandler
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
@Mixin([ResourceEnabled, MessageHandlerRegistry])
class SystemService extends Verticle implements Service {
    public final static String MY_ADDRESS = SystemService.class.name
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
                    invalidNewTimezone = "Missing or invalid new timezone value"
                    missingZoneFile = "The specified timezone file doesn't exist"
                    unknownOS = "Unable to determine Operating System"
                    failedToUpdateTZ = "Unable to determine Operating System"
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
            addHandler(svc)
        }

        // Register with the event bus
        vertx.eventBus.registerLocalHandler(MY_ADDRESS, { Message message ->
            container.logger.info "Received Message: ${message.body()}"
            def msgBody = message.body()
            if (!(msgBody instanceof Map)) {
                message.reply(RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.invalidMessagePayload"))
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
        def valid = validInfo.valid

        container.logger.info "DEBUG: message: $message"

        if (!valid) {
            return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.msgValidationFailure")
        }

        def handler = lookupHandler(message.type as String)
        if (!handler) {
            return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.unknownMessageType")
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
        loadLocalResource(new ClosureScriptAsClass(closure: myConfigObject))
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