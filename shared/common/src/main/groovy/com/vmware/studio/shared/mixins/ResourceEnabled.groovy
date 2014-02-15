package com.vmware.studio.shared.mixins

import com.vmware.studio.shared.utils.ClosureScriptAsClass
import com.vmware.studio.shared.utils.ResourceLoader
import groovy.transform.CompileStatic

/**
 * Created by samueldoyle on 2/14/14.
 * Can be used as mixin to provide resourceloader related features
 */
@CompileStatic
class ResourceEnabled {

    public void loadLocalResource(Script configClosure) {
        ResourceLoader.instance.loadConfigObject(configClosure)
    }

    /**
     * Single value from lookup
     * @param resourceKey looks up from resourceloader
     * @return
     */
    public Map RESOURCE_ERROR_RESPONSE(String resourceKey) {
        def cause = ResourceLoader.instance.getConfigProperty(resourceKey)
        [result: "error", cause: cause]
    }

    /**
     * Lookup key plus additional value
     * @param resourceKey looks up from resourceloader
     * @additionalInfo additional information appended
     * @return
     */
    public Map RESOURCE_ERROR_RESPONSE(String resourceKey, String additionalInfo) {
        def cause = ResourceLoader.instance.getConfigProperty(resourceKey) + " $additionalInfo"
        [result: "error", cause: cause]
    }

    /**
     * Just error as is
     * @param errMsg the error message
     * @return
     */
    public Map ERROR_RESPONSE(String resourceKey) {
        def cause = ResourceLoader.instance.getConfigProperty(resourceKey)
        [result: "error", cause: cause]
    }

    public Map OK_RESPONSE(data) {
        [result: "ok", data: data]
    }

}
