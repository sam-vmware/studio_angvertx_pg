package com.vmware.studio.shared.services

import groovy.transform.CompileStatic

/**
 * Created by samueldoyle on 2/14/14.
 * Place holder for now, all services should implement an interface
 */
//@Mixin([ContextConfig, CommonService])
@CompileStatic
public interface Service {
    void registerService()
    /**
     * This is the expected directory where this content should reside, which is usually the name
     * of the deployed module i.e. "com.vmware~vami-system-service~1.0"
     */
    File getExpectedRoot()

    /**
     * All services that provide content need to listen on the global "GLOBAL_RESOURCE_CHANNEL" topic
     * Messages for heartbeat and/or reannounce type to come
     * @param message
     */
    void handleGlobalResourceMessage(Map message)

    /**
     * Whatever the official name it plans on using
     * @return
     */
    String getServiceName()

    /**
     * This services address on the eventbus
     * @return
     */
    String getAddress()
}
