package com.vmware.studio.shared.mixins
import groovy.transform.CompileStatic
import com.vmware.studio.shared.services.messaging.BaseMessageHandler

/**
 * Created by samueldoyle on 2/14/14.
 * Handler registry specific functionality
 */
@CompileStatic
class MessageHandlerRegistry {
    private Map<String, BaseMessageHandler> HANDLER_REGISTRY = new HashMap<String, BaseMessageHandler>()

    public void addHandler(String type, BaseMessageHandler handler) {
        HANDLER_REGISTRY[type] = handler
    }

    public void addHandler(BaseMessageHandler handler) {
        HANDLER_REGISTRY[handler.type] = handler
    }

    public void removeHandler(String type) {
        HANDLER_REGISTRY.remove(type)
    }

    public BaseMessageHandler lookupHandler(String type) {
        HANDLER_REGISTRY[type]
    }
}
