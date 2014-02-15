package com.vmware.studio.shared.system

import groovy.util.logging.Log

@Log(value = "LOGGER")
@Singleton
class LinuxShellSupport {

    private def consumeShellCmdCommon(String command, File workingDir, String[] envp) {
        Process process = command.execute(envp, workingDir)
        def out = new StringBuffer()
        def err = new StringBuffer()
        process.consumeProcessOutput(out, err)
        return [process, out, err]
    }

    def consumeShellCmdWait(String command, String[] envp = [], File workingDir = new File(System.properties.'user.dir')) {
        LOGGER.fine "Running command: $command"
        def (process, out, err) = consumeShellCmdCommon(command, workingDir, envp)
        process.waitFor()
        return [process, out, err]
    }

    def consumeShellCmdNoWait(String command, String[] envp = [], File workingDir = new File(System.properties.'user.dir')) {
        LOGGER.fine "Running command: $command"
        consumeShellCmdCommon(command, workingDir, envp)
    }

    def executeShellCmdWait(String command, String[] envp = [], File workingDir = new File(System.properties.'user.dir')) {
        LOGGER.fine "Running command: $command"
        Process p = command.execute(envp, workingDir)
        p.waitFor()
        return p
    }

    def executeShellCmdNoWait(String command, String[] envp = [], File workingDir = new File(System.properties.'user.dir')) {
        LOGGER.fine "Running command: $command"
        command.execute(envp, workingDir)
    }
}