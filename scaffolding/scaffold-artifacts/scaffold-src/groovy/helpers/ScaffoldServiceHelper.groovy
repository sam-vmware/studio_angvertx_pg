package @modOwner@.helpers

import groovy.util.logging.Log
import com.vmware.studio.shared.services.messaging.BaseMessageHandler
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
 * ScaffoldServiceHelper
 */
@Log(value = "LOGGER")
@Mixin([ResourceEnabled])
class @serviceHelperName@ extends BaseMessageHandler {

    public static final String MY_TYPE = "HelloWorld"

    @serviceHelperName@(String myType = MY_TYPE) {
        super(myType)
    }

    def sayHello(Map message) {
        LOGGER.info "Saying Hello!"
        OK_RESPONSE("@helloMessage@" as String)
    }

    /***** Implementations Below *****/

    @Override
    Map handle(Map message) {
        // Use method reference directly to avoid any getter/setter interception nastiness
        if (!this.metaClass.respondsTo(this, message.operation as String)) {
            return RESOURCE_ERROR_RESPONSE("services.@serviceName@.errorMessages.unknownOperationType")
        }
        this.&"${message.operation}"(message)
    }
}
