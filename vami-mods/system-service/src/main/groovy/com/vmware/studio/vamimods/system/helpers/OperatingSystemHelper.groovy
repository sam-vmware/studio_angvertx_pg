package com.vmware.studio.vamimods.system.helpers

import com.vmware.studio.shared.mixins.ResourceEnabled
import com.vmware.studio.shared.services.messaging.BaseMessageHandler
import com.vmware.studio.shared.system.LinuxShellSupport
import groovy.util.logging.Log

/**
 * Created by samueldoyle on 2/11/14.
 * For performing OS specific operations such as shutdown or restart
 */
@Log(value = "LOGGER")
@Mixin(ResourceEnabled)
class OperatingSystemHelper implements BaseMessageHandler {
    public static final String MY_TYPE = "OperatingSystem"
    public static final String rebootCommand = "/sbin/shutdown -r -t 5 now"
    public static final String shutdownCommand = "/sbin/shutdown -h -t 5 now"

    def doReboot(Map message) {
        LOGGER.info "Performing actual reboot operation!"
        Process p = LinuxShellSupport.instance.executeShellCmdNoWait(rebootCommand)
        if (!p.exitValue() == 0) {
            return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.rebootCmdFailed", p.err.text)
        }
        OK_RESPONSE(rebootCommand)
    }

    def doShutdown(Map message) {
        LOGGER.info "Performing actual shutdown operation!"
        Process p = LinuxShellSupport.instance.executeShellCmdNoWait(shutdownCommand)
        if (!p.exitValue() == 0) {
            return RESOURCE_ERROR_RESPONSE("services.systemService.errorMessages.shutdownCmdFailed", p.err.text)
        }
        OK_RESPONSE(shutdownCommand)
    }

    def testDoReboot(Map message) {
        LOGGER.info "Performing simulated reboot operation!"
        LOGGER.info "Would run: $rebootCommand"
        OK_RESPONSE(rebootCommand)
    }


    def testDoShutdown(Map message) {
        LOGGER.info "Performing simulated shutdown operation!"
        LOGGER.info "Would run: $shutdownCommand"
        OK_RESPONSE(shutdownCommand)
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
