package com.vmware.studio.shared.utils

import com.vmware.studio.shared.system.LinuxShellSupport
import groovy.transform.ThreadInterrupt
import groovy.util.logging.Log
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

/**
 * Created by samueldoyle on 2/12/14.
 * Having GroovyConsole available is highly valuable although things aren't working quite correctly with this
 * After trying several attempts which seemed as if thye should have worked I tried the distribution version
 * unaltered and was still unable to set the classloader or classpath for the scripts, ongoing attempt is to
 * provide own implementation of GroovyConsole see MyGroovyConsole
 */
@Log(value = "LOGGER")
class GroovyConsoleWrapper {
    static def me = "*** ${GroovyConsoleWrapper.class.name}"

    def addJars(String jarFiles, ClassLoader theLoader) {
        def jars = jarFiles.split(':')
        jars.each { theLoader.addURL(new File(it).toURI().toURL()) }
    }

    def launchInstance(String jarFiles) {

        def groovyConsole = new groovy.ui.Console(this.class.classLoader.parent)
        groovyConsole.useScriptClassLoaderForScriptExecution = true
        def shell = new GroovyShell(this.class.classLoader.parent)

        def jars = jarFiles.split(':')
        jars.each { shell.classLoader.addURL(new File(it).toURI().toURL()) }

        groovyConsole.shell = shell
        groovyConsole.run()
    }

    def launchInstanceWithCP(targetClasspath) {
        def theLoader = this.class.classLoader.rootLoader
        targetClasspath = System.getProperty('java.class.path')

        groovy.ui.Console.metaClass.newScript = { ClassLoader parent, Binding binding ->
            println " !!!! INSIDE NEW newScript !!!!"
            println " *** CLASSPATH: $targetClasspath"
            config = new CompilerConfiguration(classpath: targetClasspath)
            if (threadInterrupt) config.addCompilationCustomizers(new ASTTransformationCustomizer(ThreadInterrupt))

            def newClassLoader = new GroovyClassLoader()
            newClassLoader.addClasspath(targetClasspath)
            shell = new GroovyShell(newClassLoader, binding, config)
            useScriptClassLoaderForScriptExecution = true
        }

        def groovyConsole = new groovy.ui.Console(theLoader)
        //groovyConsole.useScriptClassLoaderForScriptExecution = true
        groovyConsole.run()
    }

    def launchInstanceNew(args) {
        def theJars = args[1]
        def theLoader = this.class.classLoader
        addJars(theJars, theLoader)
        def groovyConsole = new MyGroovyConsole(theLoader, new Binding())
        groovyConsole.run()
    }

    def launchProcess(String[] args) {
        def commandLine = "/usr/local/java/groovy/groovy-2.2.1/bin/groovyConsole --classpath ${args[0]}"
        LOGGER.info("$me: Launching GroovyConsole: $commandLine")

        String[] envp = ["CLASSPATH=${args[0]}"]
        def (process, out, err) = LinuxShellSupport.instance.consumeShellCmdWait(commandLine, envp)
        if (process.exitValue()) {
            LOGGER.severe("Failed to launch groovyConsole: ${process.err.text}")
        }
    }

    public static void main(String[] args) {
        //new GroovyConsoleWrapper().launchInstanceNew(args)
        new GroovyConsoleWrapper().launchProcess(args)
    }
}