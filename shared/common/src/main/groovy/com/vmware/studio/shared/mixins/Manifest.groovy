package com.vmware.studio.shared.mixins

import groovy.util.logging.Log

/**
 * Created by samueldoyle on 2/15/14.
 */
@Log(value = "LOGGER")
@Mixin(ResourceEnabled)
class Manifest {

    def basicManifestValidate(Node updateNode) {
        assert updateNode instanceof Node
        for (targetNode in ["product", "vendor", "fullVersion"]) {
            if (!(updateNode."$targetNode".text())) {
                return ERROR_RESPONSE("Manifest file failed basic validation for node: $targetNode")
            }
        }
        return OK_RESPONSE()
    }

    def loadManifest(File manifestFile) {
        if (!manifestFile.canRead()) {
            return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.manifestFileNotReadable")
        }
        LOGGER.fine "Loading manifest file: ${manifestFile.name}"
        def update = new XmlParser().parse(manifestFile)
        def validateResult = basicManifestValidate(update)
        if (!(validateResult["result"] == OK)) {
            return validateResult
        }

        return OK_RESPONSE(update)
    }

}
