package com.vmware.studio.shared.system

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import groovy.util.logging.Log

/**
 * Created by samueldoyle on 2/14/14.
 * OS specific utilities
 */
@Log(value = "LOGGER")
@CompileStatic
@Singleton(strict = false, lazy = false)
class OSSupport {
    def final ME = OSSupport.class.name

    private OSSupport() {
        OSType.values().each { OSType osType ->
            LOGGER.fine("$ME: Initializing ${osType.OSName}")
        }
    }

    public OSType getOperatingSystemType() {
        Process process = LinuxShellSupport.instance.executeShellCmdWait("/bin/cat /proc/version")
        assert process.exitValue() == 0
        OSType returnType
        switch (process.text) {
            case ~/(?is).*?ubuntu.*?/: returnType = OSType.DEBIAN; break
            case ~/(?is).*?centos.*?/: returnType = OSType.CENTOS; break
            case ~/(?is).*?redhat.*?/: returnType = OSType.RH; break
            case ~/(?is).*?suse.*?/: returnType = OSType.SUSE; break
            default: returnType = OSType.UNKNOWN
        }

        returnType
    }

    public String getHostName() {
        Process process = LinuxShellSupport.instance.executeShellCmdWait("/bin/hostname")

        if (process.exitValue()) {
            LOGGER.severe("$ME: Failed to run hostname command")
            return null
        }

        process.text
    }
}
