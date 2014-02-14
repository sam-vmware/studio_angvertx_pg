package com.vmware.studio.vamimods.system.helpers

import com.vmware.studio.services.messaging.BaseMessageHandler
import com.vmware.studio.shared.system.LinuxShellSupport
import com.vmware.studio.utils.ResourceLoader
import org.vertx.groovy.core.Vertx

/**
 * Created by samueldoyle on 2/11/14.
 */
class TimeZoneMessageHandler implements BaseMessageHandler {
    public static final String MY_TYPE = "TimeZone"
    def final tzFile = "/etc/timezone"
    def final vertx = Vertx.newVertx()
    def tzPattern = ~$//usr/share/zoneinfo/?/$

    def tzProps = { String path ->
        vertx.fileSystem.propsSync(path)
    }

    /**
     * Get TimeZone value
     * type: MY_TYPE
     * operation: get
     * @param message
     * @return
     */
    def get(Map message) {
        def tzFileProps = tzProps(tzFile)
        def returnMsg = [:]
        def tzText = ""

        // see http://stackoverflow.com/questions/12521114/getting-the-canonical-time-zone-name-in-shell-script
        if (tzFileProps.isRegularFile()) {
            def process = LinuxShellSupport.executeShellCmdWait("cat /etc/timezone")
            if (process.exitValue()) {
                returnMsg += ERROR_RESPONSE(process.err.text)
            } else {
                tzText = process.text
            }
        } else if (tzFileProps.isSymbolicLink()) {
            def process = LinuxShellSupport.executeShellCmdWait("readlink /etc/localtime")
            if (process.exitValue()) {
                returnMsg += ERROR_RESPONSE(process.err.text)
            } else {
                tzText = process.text.replaceAll(tzPattern, "")
            }
        }

        !returnMsg.isEmpty() ? returnMsg : OK_RESPONSE(tzText)
    }

    def setTimeZone(Map message) {

    }

    Map handle(Map message) {
        // Use method reference directly to avoid any getter/setter interception nastiness
        if (! this.metaClass.respondsTo(this, message.body.operation as String)) {
            return BasemessageHandler.ERROR_RESPONSE(
                    ResourceLoader.getProperty("services.systemService.errorMessages.unknownOperationType")
            )
        }
        return this.".&${message.body.operation}"(message)
    }

    String getType() {
        return MY_TYPE
    }
}
