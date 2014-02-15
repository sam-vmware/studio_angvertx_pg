package com.vmware.studio.shared.services.messaging

/**
 * Created by samueldoyle on 2/11/14.
 */
public interface BaseMessageHandler {
    Map handle(Map message)
    String getType()
}
