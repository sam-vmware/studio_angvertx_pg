package @modOwner@

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
 * ScaffoldService
 */
@Mixin([ResourceEnabled, MessageHandlerRegistry, ContextConfig, CommonService])
class @serviceName@ extends Verticle implements Service {
    final String CURRENT_ENVIRONMENT = System.properties["env"] ?: "dev"

    def myConfigObject = {
        services {
            @serviceName@ {
                handlers = [
                    [name: "HelloWorld", FQCN: "@modOwner@.helpers.@serviceHelperName@", enabled: true]
                ]
                // This is the directory we expect to find be available from the start directory
                // which should be the VERTX_MODS directory e.g.
                // MY_ROOT_SHOULD_BE_HERE = $VERTX_MODS/$expectedRootMatch
                expectedRootMatch = "@modOwner@~@modName@~1.0"
                errorMessages {
                    unknownMessageType = "Unknown message type received"
                    unknownOperationType = "Unknown operation type received"
                    msgValidationFailure = "Message validation failed"
                    invalidMessagePayload = "Invalid message body payload type received"
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
        def handlers = GET_CONFIG().services.@serviceName@.handlers
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
                message.reply(RESOURCE_ERROR_RESPONSE("services.@serviceName@.errorMessages.invalidMessagePayload"))
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
            return RESOURCE_ERROR_RESPONSE("services.@serviceName@.errorMessages.msgValidationFailure")
        }

        def handler = lookupHandler(handlerType as String)
        if (!handler) {
            return RESOURCE_ERROR_RESPONSE("services.@serviceName@.errorMessages.unknownMessageType")
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

        def resourceSvcRegistrationMsg = [
            data     : [
                title     : "@serviceName@",
                isDisabled: false,
                svcName   : "@serviceName@",
                webRootDir: "",
                indexFile : "index.html"
            ],
            type     : "ResourceRequestHandler",
            operation: "addNewService"
        ]

        resourceSvcRegistrationMsg.data.webRootDir = myWebRootDir as String

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
        new File(GET_CONFIG().services.@serviceName@.expectedRootMatch)
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
        "@serviceName@"
    }

    @Override
    String getAddress() {
        "@modName@.@serviceName@"
    }
}