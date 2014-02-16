package com.vmware.studio.shared.system

import groovy.util.logging.Log
import org.vertx.groovy.core.Vertx


/**
 * Created by samueldoyle on 2/14/14.
 * For doing file io specific operations
 * When possible in vertx all io should flow through their apis and ideally use
 * the async version, to be flushed out later
 */
@Log(value = "LOGGER")
@Singleton
class FSSupport {
    private final ME = FSSupport.class.name
    public final LN = "/bin/ln"
    public final LN_OPTIONS = "-nfs"
    final vertx = Vertx.newVertx()

    /*** ALL THESE OPERATIONS SHOULD USE THE ASYNC VERT.X APIS
     * SEE: http://vertx.io/core_manual_groovy.html#file-system
     ***/

    /**
     * Simply checks if a file exists and written, if so, updates with the results
     * of the passed closure
     * @param file
     * @param processText
     * @return
     */
    boolean processFileInplace(File file, Closure processText) {
        if (!file || !file.exists() || !file.canWrite()) {
            LOGGER.fine("File ${file.name} cannot be written")
            return false
        }

        def text = file.text
        file.write(processText(text))
        true
    }

    /**
     * Symbolic links or relinks existing link to the give source
     * @param sourceFile
     * @param link
     * @return - the shells exit code i.e != 0 is error
     */
    int linkOrRelink(File sourceFile, File link) {
        def cmd = $/$LN $LN_OPTIONS ${sourceFile.absolutePath} ${link.absolutePath}/$
        LOGGER.info("$ME: Running shell cmd: $cmd")
        return LinuxShellSupport.instance.executeShellCmdWait(cmd).exitValue()
    }
}
