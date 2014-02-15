package com.vmware.studio.vamimods.system.helpers

import com.vmware.studio.shared.mixins.Manifest
import com.vmware.studio.shared.mixins.ResourceEnabled
import groovy.util.logging.Log

/**
 * Created by samueldoyle on 2/14/14.
 */
@Log(value = "LOGGER")
@Mixin([ResourceEnabled, Manifest])
class InformationMessageHandler {
    final manifestXMLFile = "/opt/vmware/etc/appliance-manifest.xml"

}
