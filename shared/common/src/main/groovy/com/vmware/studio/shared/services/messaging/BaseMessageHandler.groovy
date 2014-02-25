package com.vmware.studio.shared.services.messaging

import groovy.transform.CompileStatic

/**
 * Created by samueldoyle on 2/11/14.
 */
@CompileStatic
public abstract class BaseMessageHandler {
    protected String type

    private BaseMessageHandler() {}

    public BaseMessageHandler(String type) {
        this.type = type
    }

    public String getType() { this.type }

    abstract Map handle(Map message)
}
