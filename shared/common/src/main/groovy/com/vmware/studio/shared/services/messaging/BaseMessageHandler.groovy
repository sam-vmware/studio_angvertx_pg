package com.vmware.studio.shared.services.messaging

import groovy.transform.CompileStatic

/**
 * Created by samueldoyle on 2/11/14.
 */
@CompileStatic
public abstract class BaseMessageHandler {
    protected String type
    protected final List UNWANTED_KEYS = ['operation', 'type'].asImmutable()

    private BaseMessageHandler() {}

    public BaseMessageHandler(String type) {
        this.type = type
    }

    public String getType() { this.type }

    Map stripPostProcessKeys(Map message) {
        for (key in UNWANTED_KEYS) {
            message.remove(key)
        }

        message
    }

    abstract Map handle(Map message)
}
