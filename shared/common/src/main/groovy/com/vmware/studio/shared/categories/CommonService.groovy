package com.vmware.studio.shared.categories

import com.vmware.studio.shared.mixins.ContextConfig
import com.vmware.studio.shared.mixins.ResourceEnabled
import com.vmware.studio.shared.services.Service
import com.vmware.studio.shared.services.messaging.types.GlobalMsgTypes
import com.vmware.studio.shared.utils.GlobalServiceConfig
import org.vertx.groovy.core.eventbus.Message

/**
 * Created by samueldoyle
 * Automatically provide some common functionality to all services implementing the interface
 */
@Category(Service)
@Mixin([ContextConfig, ResourceEnabled])
class CommonService {
    public static final Set GLOBAL_MSG_REQUIRED_KEYS = ["type", "originatorAddress"].asImmutable()

    /**
     * For web content we want to verify we have our actual package one level below the mod directory
     */
    void VERIFY_INSTALL_ROOT() {

        // Verify that we are out where we think we should be
        def expectedRoot = this.expectedRoot
        def workingDir = GET_BASE_ROOT()
        def expectedRootAbs = new File(workingDir, expectedRoot.name)
        def container = GET_CONTAINER()
        container.logger.info "Verifying the expected root: $expectedRootAbs"

        assert expectedRootAbs.isDirectory() && expectedRootAbs.canRead():
            "ERROR: expectedRoot: $expectedRootAbs.absolutePath() is not available or is not an accessible directory"
    }

    /**
     * Handles the subscribe to global resource channel that all services that have web content should do
     */
    void GLOBAL_RESOURCE_SUBSCRIBE() {
        def vertx = GET_VERTX()
        def container = GET_CONTAINER()

        def globalChannel = GlobalServiceConfig.instance.globalServiceCommonConfig.service.globalChannel
        vertx.eventBus.registerHandler(globalChannel as String, { Message message ->
            container.logger.info "Received Global Resource Message: ${message.body()}"
            def msgBody = message.body()
            if (!(msgBody instanceof Map)) {
                message.reply(RESOURCE_ERROR_RESPONSE("Message type or payload is invalid"))
            } else if(!msgBody.keySet().containsAll(GLOBAL_MSG_REQUIRED_KEYS)) {
                message.reply(RESOURCE_ERROR_RESPONSE("Message was not properly formatted, missing required keys"))
            } else if (!(msgBody.type as GlobalMsgTypes)) {
                message.reply(RESOURCE_ERROR_RESPONSE("Uknown GLOBAL MESSAGE TYPE", msgBody.type))
            } else {
                this.handleGlobalResourceMessage(msgBody)
            }
        })
    }
}
