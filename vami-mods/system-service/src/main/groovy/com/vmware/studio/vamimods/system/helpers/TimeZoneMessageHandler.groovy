package com.vmware.studio.vamimods.system.helpers

import com.vmware.studio.shared.mixins.ResourceEnabled
import com.vmware.studio.shared.services.messaging.BaseMessageHandler
import com.vmware.studio.shared.system.FSSupport
import com.vmware.studio.shared.system.LinuxShellSupport
import com.vmware.studio.shared.system.OSSupport
import com.vmware.studio.shared.system.OSType
import groovy.util.logging.Log
import org.vertx.groovy.core.Vertx

/**
 * Created by samueldoyle on 2/11/14.
 */
@Log(value = "LOGGER")
@Mixin(ResourceEnabled)
class TimeZoneMessageHandler implements BaseMessageHandler {
    private final ME = this.class.name
    public final String MY_TYPE = "TimeZone"
    final vertx = Vertx.newVertx()
    final NL = System.properties['line.separator']
    final tzFile = "/etc/timezone"
    final clockFile = "/etc/sysconfig/clock"
    final localTimeFile = "/etc/localtime"
    final tmplocalTimeFile = "/tmp/localtime"
    final zoneInfoDir = "/usr/share/zoneinfo/"
    final tzGetPattern = ~$/${zoneInfoDir}?/$
    final tzSetPattern = ~$/(^([a-zA-Z0-9\-\+_]*/{0,1})+$)/$

    def tzProps = { String path ->
        vertx.fileSystem.propsSync(path)
    }

    final def localTimeUpdater = { File newLocalTimeFile, File ltFile = new File(localTimeFile) ->
        FSSupport.instance.linkOrRelink(newLocalTimeFile, ltFile)
    }

    // For debian just replace the /etc/timezone
    final def debianTZUpdater = { newTZValue, fileText ->
        newTZValue
    }

    // For RH/CentOS/SLES update the text (should be from /etc/sysconfig/clock) with the new ZONE
    final def rhCentSuseTZUpdater = { newTZValue, fileText ->
        def lines = fileText.split(NL)
        lines.collect {
            it =~ /.*?ZONE/ ? /ZONE=${newTZValue}/ : it
        }.join(NL)
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
            def process = LinuxShellSupport.instance.executeShellCmdWait("cat /etc/timezone")
            if (process.exitValue()) {
                returnMsg += ERROR_RESPONSE(process.err.text)
            } else {
                tzText = process.text
            }
        } else if (tzFileProps.isSymbolicLink()) {
            def process = LinuxShellSupport.instance.executeShellCmdWait("readlink /etc/localtime")
            if (process.exitValue()) {
                returnMsg += ERROR_RESPONSE(process.err.text)
            } else {
                tzText = process.text.replaceAll(tzGetPattern, "")
            }
        }

        !returnMsg.isEmpty() ? returnMsg : OK_RESPONSE(tzText)
    }

    def setTimeZone(Map message) {
        def data = message.operation.data
        def newTimeZone = data.newTimeZone
        if (!newTimeZone || !tzSetPattern.matcher(newTimeZone).matches()) {
            return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.invalidNewTimezone")
        }

        def newLocalTimeFile = new File("/usr/share/zoneinfo/$newTimeZone")
        if (!newLocalTimeFile.canRead()) {
            return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.missingZoneFile", newTimeZone)
        }

        def targetUpdater
        String targetFileToUpdate
        switch (OSSupport.instance.operatingSystemType) {
            case OSType.DEBIAN:
                // reprocess /etc/timezone with debian processor
                targetUpdater = debianTZUpdater
                targetFileToUpdate = tzFile
                break
            case OSType.RH:
            case OSType.CENTOS:
            case OSType.SUSE:
                // reprocess /etc/sysconfig/clock with rh processor
                targetUpdater = rhCentSuseTZUpdater
                targetFileToUpdate = clockFile
                break
            default:
                return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.unknownOS")
        }

        // Update the target content file using the correct updater
        boolean result = FSSupport.instance.processFileInplace(new File(targetFileToUpdate), targetUpdater.curry(newTimeZone))
        if (!result) {
            return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.failedToUpdateTZ")
        }

        if (localTimeUpdater(newLocalTimeFile) != 0) {
            return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.failedToUpdateTZ")
        }

        OK_RESPONSE(newTimeZone)
    }

    // Until I figure out how to properly deal with operations in test
    /**
     * Set the timzezone
     * @param message must have *message.data.newTimeZone*
     * @return
     */
    def testSetTimeZone(Map message) {

        def newTimeZone = message.data.newTimeZone

        def msg = $/
           $ME DEBUG MSG START
           ---------------
           INSIDE: TimeZoneMessageHandler.testSetTimeZone

           TimeZoneMessageHandler.testSetTimeZone
           message = ${message}

           $ME DEBUG MSG END
           ---------------
        /$

        LOGGER.info msg

        if (!newTimeZone || !tzSetPattern.matcher(newTimeZone).matches()) {
            return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.invalidNewTimezone")
        }

        def newLocalTimeFile = new File("/usr/share/zoneinfo/$newTimeZone")
        if (!newLocalTimeFile.canRead()) {
            return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.missingZoneFile")
        }
        LOGGER.info "$ME Setting new timezone: $newTimeZone"

        def targetUpdater
        String targetFileToUpdate
        switch (OSSupport.instance.operatingSystemType) {
            case OSType.DEBIAN:
                // reprocess /etc/timezone with debian processor
                targetUpdater = debianTZUpdater
                targetFileToUpdate = tzFile
                break
            case OSType.RH:
            case OSType.CENTOS:
            case OSType.SUSE:
                // reprocess /etc/sysconfig/clock with rh processor
                targetUpdater = rhCentSuseTZUpdater
                targetFileToUpdate = clockFile
                break
            default:
                return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.unknownOS")
        }

        /** MOCK FOR TEST **/
        def fsSupportMock = new Expando()
        fsSupportMock.processFileInplace = { File file, Closure processText ->
            if (!file || !file.exists() || !file.canWrite()) {
                LOGGER.info "$ME File ${file.name} cannot be written"
                //return false
            }

            def text = file.text
            def newText = processText(text)
            LOGGER.info "$ME MOCKED: would run -> file.write(processText($newText))"
            true
        }
        /** END MOCK FOR TEST **/

        // Use mock so not actually do update on the target content file using the correct updater
        boolean result = fsSupportMock.processFileInplace(new File(targetFileToUpdate), targetUpdater.curry(newTimeZone))
        if (!result) {
            return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.failedToUpdateTZ")
        }

        // Replace linking the real local time with a tmp
        if (localTimeUpdater(newLocalTimeFile, new File(tmplocalTimeFile)) != 0) {
            return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.failedToUpdateTZ")
        }

        OK_RESPONSE(newTimeZone)
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
