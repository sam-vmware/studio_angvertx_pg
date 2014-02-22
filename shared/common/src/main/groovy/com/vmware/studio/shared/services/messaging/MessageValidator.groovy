package com.vmware.studio.shared.services.messaging

import groovy.util.logging.Log

/**
 * Created by samueldoyle on 2/13/14.
 * Do some basic message validation
 */
@Singleton
@Log(value = "LOGGER")
class MessageValidator {
    public final Set REQUIRED_KEYS = ["type", "operation"].asImmutable()

    public Map validate(Map message, Set requiredKeys = REQUIRED_KEYS) {
        def response = [valid: true]

        def commons = requiredKeys.intersect(message.keySet())
        if (!commons.containsAll(requiredKeys)) {
            response.valid = false
        }
/*        def difference = REQUIRED_KEYS.plus(message.keySet())
        difference.removeAll(commons)
        if (difference.size()) {
            response = [invalid:difference]
        }*/

        response
    }
}
