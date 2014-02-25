package com.vmware.studio.shared.system

import groovy.util.logging.Log

/**
 * Created by samueldoyle
 * Intended to provide support for dealing with shell commands
 * This should be abstracted so that it isn't Linux specific
 */
@Log(value = "LOGGER")
@Singleton(lazy = true)
class LinuxShellSupport {

    private def consumeShellCmdCommon(String command, File workingDir, String[] envp) {
        Process process = command.execute(envp, workingDir)
        def out = new StringBuffer()
        def err = new StringBuffer()
        process.consumeProcessOutput(out, err)
        return [process, out, err]
    }

    List consumeShellCmdWait(String command, String[] envp = [], File workingDir = new File(System.properties.'user.dir')) {
        LOGGER.fine "Running command: $command"
        def (process, out, err) = consumeShellCmdCommon(command, workingDir, envp)
        process.waitFor()
        return [process, out, err]
    }

    List consumeShellCmdNoWait(String command, String[] envp = [], File workingDir = new File(System.properties.'user.dir')) {
        LOGGER.fine "Running command: $command"
        consumeShellCmdCommon(command, workingDir, envp)
    }

    Process executeShellCmdWait(String command, String[] envp = [], File workingDir = new File(System.properties.'user.dir')) {
        LOGGER.fine "Running command: $command"
        Process p = command.execute(envp, workingDir)
        p.waitFor()
        return p
    }

    Process executeShellCmdNoWait(String command, String[] envp = [], File workingDir = new File(System.properties.'user.dir')) {
        LOGGER.fine "Running command: $command"
        command.execute(envp, workingDir)
    }
}