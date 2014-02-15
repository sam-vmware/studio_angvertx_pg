package com.vmware.studio.vamimods.system.helpers

import com.vmware.studio.shared.mixins.Manifest
import com.vmware.studio.shared.mixins.ResourceEnabled
import com.vmware.studio.shared.services.messaging.BaseMessageHandler
import com.vmware.studio.shared.system.OSSupport
import groovy.util.logging.Log

/**
 * Created by samueldoyle on 2/14/14.
 */
@Log(value = "LOGGER")
@Mixin([ResourceEnabled, Manifest])
class InformationMessageHandler implements BaseMessageHandler {
    public static final String MY_TYPE = "SystemInformation"
    public final manifestXMLFile = "/opt/vmware/etc/appliance-manifest.xml"

    /**
     * Get basic system information
     * @return
     * from update manifest.xml - product, vendor, version
     * from system - hostname, osname
     */
    def getSystemInformation(Map message, File manifestFile = new File(manifestXMLFile)) {
        def update = loadManifest(manifestFile)

        def data = [
            product: "${update.product.text()}",
            vendor : "${update.vendor.text()}",
            version: "${update.fullVersion.text()}"
        ]

        def hostName = OSSupport.instance.hostName
        assert hostName
        def osName = OSSupport.instance.operatingSystemType.OSName

        data += [
            hostName: "$hostName",
            osName  : "$osName"
        ]

        OK_RESPONSE(data)
    }

    def testGetSystemInformation(Map message) {
        File tmpManifestFile = File.createTempFile("manifest", ".xml")
        tmpManifestFile.deleteOnExit()
        tmpManifestFile.text =
            $/<?xml version="1.0"?> <update xmlns:vadk="http://www.vmware.com/schema/vadk" xmlns:ovf="http://schemas.dmtf.org/ovf/envelope/1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:vmw="http://www.vmware.com/schema/ovf"> <product>VMware Studio</product> <version>2.7.0.0</version> <fullVersion>2.7.0.0 Build 140209153907</fullVersion> <vendor>VMware, Inc.</vendor> <vendorUUID>706ee0c0-b51c-11de-8a39-0800200c9a66</vendorUUID> <productRID>9aa20093-83ac-4707-858e-2ff30c043595</productRID> <vendorURL>http://www.vmware.com</vendorURL> <productURL/> <supportURL/> <releaseDate>20140209220754.000000+000</releaseDate> <description>VMware Studio 2.7.0.0</description> <EULAList showPolicy="" introducedVersion=""> <EULA>EULA for upgrading to VMware Studio 2.7.0.0.</EULA> </EULAList> <UpdateInfoList> <UpdateInfo introduced-version="2.7.0.0" category="fix" severity="important" affected-versions="" description="VMware Studio 2014" reference-type="vmwarekb" reference-id="123" reference-url="http://www.vmware.com/kb/123"/> </UpdateInfoList> <preInstallScript/> <postInstallScript/> <Network protocols="IPv4"/> </update>/$
        getSystemInformation(message, tmpManifestFile)
    }

    /***** Implementations Below *****/

    @Override
    String getType() {
        MY_TYPE
    }

    @Override
    Map handle(Map message) {
        // Use method reference directly to avoid any getter/setter interception nastiness
        if (!this.metaClass.respondsTo(this, message.operation as String)) {
            return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.unknownOperationType")
        }
        this.&"${message.operation}"(message)
    }
}
