package com.vmware.studio.vamimods.system.resources

/**
 * Created by samueldoyle on 2/13/14.
 * Currently not used until classloader stuff worked out
 */

services {
    systemService {
        environments {
            development {
                bootStrapVerticle="groovy:com.vmware.studio.vamimods.system.SystemServiceBootstrap"
                errorMessages {
                    unknownMessageType = "Unknown message type received"
                    unknownOperationType = "Unknown operation type received"
                    msgValidationFailure = "Message validation failed"
                    invalidMessagePayload = "Invalid message body payload type received"
                    invalidNewTimezone = "Missing or invalid new timezone value"
                    missingZoneFile = "The specified timezone file doesn't exist"
                    unknownOS = "Unable to determine Operating System"
                    failedToUpdateTZ = "Unable to determine Operating System"
                }
            }
        }
    }
}