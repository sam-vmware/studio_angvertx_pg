package com.vmware.studio.shared.system

import groovy.transform.CompileStatic
import groovy.util.logging.Log

/**
 * Created by samueldoyle on 2/14/14.
 * OS specific utilities
 */
@Log(value = "LOGGER")
@CompileStatic
@Singleton
class OSSupport {
    enum OS_TYPE {
        DEBIAN("Ubuntu"), RH("Red Hat"), CENTOS("CentOS"), SUSE("SUSE/SLES"), UNKNOWN("Unknown")
        private final String osName
        OS_TYPE(String osName) { this.osName = osName }
        public String getOSName() { return this.osName }
    }

    public OS_TYPE getOperatingSystemType() {
        Process process = LinuxShellSupport.instance.executeShellCmdWait("cat /proc/version")
        OS_TYPE returnType
        switch(process.text) {
            case ~/(?is).*?ubuntu.*?/: returnType = OS_TYPE.DEBIAN; break
            case ~/(?is).*?centos.*?/: returnType = OS_TYPE.CENTOS; break
            case ~/(?is).*?redhat.*?/: returnType = OS_TYPE.RH; break
            case ~/(?is).*?suse.*?/: returnType = OS_TYPE.SUSE; break
            default: returnType = OS_TYPE.UNKNOWN
        }

        returnType
    }
}
