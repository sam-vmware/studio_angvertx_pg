package com.vmware.studio.vamimods.system.resources

/**
 * Created by samueldoyle on 2/13/14.
 * Currently not used until classloader stuff worked out
 */

services {
    systemService {
        bootStrapVerticle = "groovy:com.vmware.studio.vamimods.system.SystemServiceBootstrap"
        handlers = [
            [name: "TimeZoneMessageHandler", FQCN: "com.vmware.studio.vamimods.system.helpers.TimeZoneMessageHandler", enabled: true],
            [name: "InformationMessageHandler", FQCN: "com.vmware.studio.vamimods.system.helpers.InformationMessageHandler", enabled: true],
            [name: "OperatingSystemHelper", FQCN: "com.vmware.studio.vamimods.system.helpers.OperatingSystemHelper", enabled: true]
        ]
        errorMessages {
            unknownMessageType = "Unknown message type received"
            unknownOperationType = "Unknown operation type received"
            msgValidationFailure = "Message validation failed"
            invalidMessagePayload = "Invalid message body payload type received"
            invalidNewTimezone = "Missing or invalid new timezone value"
            missingZoneFile = "The specified timezone file doesn't exist"
            unknownOS = "Unable to determine Operating System"
            failedToUpdateTZ = "Unable to determine Operating System"
            manifestFileNotReadable = "The provided manifest file is not accessible : "
            rebootCmdFailed = "Failed to perform reboot operation : "
            shutdownCmdFailed = "Failed to perform shutdown operation : "
        }
    }
}
environments {
    dev {
    }
    test {
    }
    prod {
    }
}
