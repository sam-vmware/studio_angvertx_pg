package com.vmware.studio.shared.mixins

import groovy.transform.CompileStatic
import groovy.util.logging.Log

/**
 * Created by samueldoyle on 2/15/14.
 */
@Log(value = "LOGGER")
@CompileStatic
class Manifest {

    Node loadManifest(File manifestFile) {
        assert manifestFile.canRead()
        LOGGER.fine "Loading manifest file: ${manifestFile.name}"
        def update = new XmlParser().parse(manifestFile)
        assert update instanceof Node

        update
    }

}
