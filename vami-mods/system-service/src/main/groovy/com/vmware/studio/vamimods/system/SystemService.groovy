package com.vmware.studio.vamimods.system

import com.vmware.studio.shared.mixins.MessageHandlerRegistry
import com.vmware.studio.shared.mixins.ResourceEnabled
import com.vmware.studio.shared.services.Service
import com.vmware.studio.shared.services.messaging.MessageValidator
import com.vmware.studio.shared.utils.ClosureScriptAsClass
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
    private final ME = this.class.name
    public final static String MY_ADDRESS = "vami.SystemService"
    // private static String myConfigObject = "com.vmware.studio.vamimods.system.resources.SystemService"

    // Until I can figure out how to deal with the Vert.X classloader stuff
    // Belongs in resources/SystemServiceConfig
    def myConfigObject = {
        services {
            systemService {
                bootStrapVerticle = "groovy:com.vmware.studio.vamimods.system.SystemServiceBootstrap"
                handlers = [
                    [name: "TimeZoneMessageHandler", FQCN: "com.vmware.studio.vamimods.system.helpers.TimeZoneMessageHandler", enabled: true],
                    [name: "InformationMessageHandler", FQCN: "com.vmware.studio.vamimods.system.helpers.InformationMessageHandler", enabled: true],
                    [name: "OperatingSystemHelper", FQCN: "com.vmware.studio.vamimods.system.helpers.OperatingSystemHelper", enabled: true]
                ]
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
                instance = this.class.classLoader.loadClass(svc.FQCN, true)?.newInstance()
                addHandler(instance)
            } else {
                container.logger.info "Skpping disabled service ${svc.name} for registry"
            }
        }

        // Register with the event bus
        container.logger.info "Registering local service address handler @ $MY_ADDRESS"
        vertx.eventBus.registerLocalHandler(MY_ADDRESS, { Message message ->
            container.logger.info "$ME Received Message: ${message.body()}"
            def msgBody = message.body()
            if (!(msgBody instanceof Map)) {
                message.reply(RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.invalidMessagePayload"))
            } else {
                def replyMessage = handleMessage(message.body())
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
    def handleMessage(message) {
        // Basic failfast validation
        def validInfo = MessageValidator.instance.validate(message)
        def valid = validInfo.valid

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
        container.logger.info "$ME Deployment succeeded for: ${this.class.name}"

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