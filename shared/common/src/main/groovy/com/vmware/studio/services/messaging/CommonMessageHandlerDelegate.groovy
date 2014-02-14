package com.vmware.studio.services.messaging

import com.vmware.studio.utils.ResourceLoader

/**
 * Created by samueldoyle on 2/13/14.
 * Deletage for common message handler functionality, gives us a way to extend
 * without having to extend
 */
class CommonMessageHandlerDelegate implements BaseMessageHandler {

    Map handle(Map message) {
        // Use method reference directly to avoid any getter/setter interception nastiness
        if (!this.metaClass.respondsTo(this, message.operation as String)) {
            return ERROR_RESPONSE(
                    ResourceLoader.instance.getConfigProperty("services.systemService.errorMessages.unknownOperationType")
            )
        }
        return this.&"${message.operation}"(message)
    }

    String getType() {
        return MY_TYPE
    }
}
