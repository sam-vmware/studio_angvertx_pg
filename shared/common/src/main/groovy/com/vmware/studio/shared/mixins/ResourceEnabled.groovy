package com.vmware.studio.shared.mixins

import com.vmware.studio.shared.utils.ResourceLoader
import groovy.transform.CompileStatic

/**
 * Created by samueldoyle on 2/14/14.
 * Can be used as mixin to provide resourceloader related features
 */
@CompileStatic
class ResourceEnabled {
    public static final String OK = "ok"
    public static final String ERROR = "error"

    public void loadLocalResource(Script configClosure, String environment = "dev") {
        ResourceLoader.instance.loadConfigObject(configClosure, environment)
    }

    /**
     * Simply lookup given key
     * @param resourceKey - c
     * @return
     */
    def LOOKUP_CONFIG(String resourceKey) {
        ResourceLoader.instance.getConfigProperty(resourceKey)
    }

    /**
     * Just get the raw config object
     * @return
     */
    def GET_CONFIG() {
        ResourceLoader.instance.getConfigObject()
    }

    /**
     * Single value from lookup
     * @param resourceKey looks up from resourceloader
     * @return
     */
    public Map RESOURCE_ERROR_RESPONSE(String resourceKey) {
        def cause = ResourceLoader.instance.getConfigProperty(resourceKey)
        [result: ERROR, cause: cause]
    }

    /**
     * Lookup key plus additional value
     * @param resourceKey looks up from resourceloader
     * @additionalInfo additional information appended
     * @return
     */
    public Map RESOURCE_ERROR_RESPONSE(String resourceKey, String additionalInfo) {
        def cause = ResourceLoader.instance.getConfigProperty(resourceKey) + " $additionalInfo"
        [result: ERROR, cause: cause]
    }

    /**
     * Error with no cause
     * @return
     */
    public Map ERROR_RESPONSE() {
        [result: ERROR]
    }

    /**
     * Just error as is
     * @param errMsg the error message
     * @return
     */
    public Map ERROR_RESPONSE(String cause) {
        [result: ERROR, cause: cause]
    }

    public Map ERROR_RESPONSE(Map errorData) {
        [result: ERROR, errorData: errorData]
    }

    /** Ok with no data
     * @return
     */
    public Map OK_RESPONSE() {
        [result: OK]
    }

    public Map OK_RESPONSE(data) {
        [result: OK, data: data]
    }
}
