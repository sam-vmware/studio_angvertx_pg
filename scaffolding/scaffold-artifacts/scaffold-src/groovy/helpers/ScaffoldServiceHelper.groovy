package @modOwner@.helpers

import com.vmware.studio.shared.mixins.ResourceEnabled
import com.vmware.studio.shared.services.messaging.BaseMessageHandler
import groovy.util.logging.Log

/**
 * ScaffoldServiceHelper
 */
@Log(value = "LOGGER")
@Mixin(ResourceEnabled)
class @serviceHelperName@ extends BaseMessageHandler {

    public static final String ME = "@serviceHelperName@"

    def sayHello(Map message) {
        LOGGER.info "Saying Hello!"
        OK_RESPONSE("Hello From Vert.x Service $ME !!")
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
