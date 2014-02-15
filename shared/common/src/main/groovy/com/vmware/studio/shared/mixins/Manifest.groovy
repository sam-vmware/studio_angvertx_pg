package com.vmware.studio.shared.mixins

import groovy.util.logging.Log

/**
 * Created by samueldoyle on 2/15/14.
 */
@Log(value = "LOGGER")
class Manifest {

    def basicManifestValidate(Node updateNode) {
        assert updateNode instanceof Node
        for (targetNode in ["product", "vendor", "fullVersion"]) {
            assert updateNode."$targetNode".text()
        }
    }

    def loadManifest(File manifestFile) {
        assert manifestFile.canRead()
        LOGGER.fine "Loading manifest file: ${manifestFile.name}"
        def update = new XmlParser().parse(manifestFile)
        basicManifestValidate(update)

        update
    }

}
