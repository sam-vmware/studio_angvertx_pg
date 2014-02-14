package com.vmware.studio.services.messaging

/**
 * Created by samueldoyle on 2/13/14.
 * Do some basic message validation
 */
class MessageValidator {
    public static final Set REQUIRED_KEYS = ["type", "operation"].asImmutable()

    public static Map validate(Map message) {
        def response = [valid:""]

        def commons = REQUIRED_KEYS.intersect(message.keySet())
        def difference = REQUIRED_KEYS.plus(message.keySet())
        difference.removeAll(commons)

        if (difference.size()) {
            response = [invalid:difference]
        }

        return response
    }
}
