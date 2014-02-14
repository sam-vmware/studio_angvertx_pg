package com.vmware.studio.services.messaging

/**
 * Created by samueldoyle on 2/11/14.
 */
public interface BaseMessageHandler {
    public static ERROR_RESPONSE = { cause ->
        [result: "error", cause: cause]
    }
    public static OK_RESPONSE = { data ->
        [result: "ok", data: data]
    }

    Map handle(Map message)
    String getType()
}