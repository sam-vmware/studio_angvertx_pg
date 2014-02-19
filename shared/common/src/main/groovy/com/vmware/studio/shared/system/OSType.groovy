package com.vmware.studio.shared.system

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

/**
 * Created by samueldoyle on 2/18/14.
 */
@CompileStatic
@TupleConstructor(includeFields = true)
public enum OSType {
    DEBIAN("Ubuntu"), RH("Red Hat"), CENTOS("CentOS"), SUSE("SUSE/SLES"), UNKNOWN("Unknown")
    private final String theName

    private OSType(String itsName) { this.theName = itsName }

    String getOSName() { return this.theName }
}