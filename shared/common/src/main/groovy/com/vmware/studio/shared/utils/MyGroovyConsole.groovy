package com.vmware.studio.shared.utils

import groovy.transform.ThreadInterrupt
import groovy.ui.HistoryRecord
import groovy.util.logging.Log
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

import javax.swing.SwingUtilities

/**
 * Created by samueldoyle on 2/12/14.
 * Whatever, this is a mess ATM
 */
@Log(value = "LOGGER")
class MyGroovyConsole extends groovy.ui.Console {

    def myParent
    def myBinding
    MyGroovyConsole(ClassLoader parent, Binding binding) {
       super(parent, binding)
       myParent = parent
       myBinding = binding
    }

    void newScript(ClassLoader parent, Binding binding) {
        config = new CompilerConfiguration(classpath:System.getProperty('java.class.path'))
        if (threadInterrupt) config.addCompilationCustomizers(new ASTTransformationCustomizer(ThreadInterrupt))
        //shell = new GroovyShell(parent, binding, config)
        shell = new GroovyShell(parent)
    }
    void newScript() {
        this.newScript(myParent, myBinding)
    }

    void runScript(EventObject evt = null) {
        if (scriptRunning) {
            statusLabel.text = 'Cannot run script now as a script is already running. Please wait or use "Interrupt Script" option.'
            return
        }
        scriptRunning = true
        interruptAction.enabled = true
        stackOverFlowError = false // reset this flag before running a script
        def endLine = System.getProperty('line.separator')
        def record = new HistoryRecord(allText: inputArea.getText().replaceAll(endLine, '\n'),
                selectionStart: textSelectionStart, selectionEnd: textSelectionEnd)
        addToHistory(record)
        pendingRecord = new HistoryRecord(allText: '', selectionStart: 0, selectionEnd: 0)

        clearOutput()

        // Print the input text
        if (showScriptInOutput) {
            for (line in record.getTextToRun(false).tokenize("\n")) {
                appendOutputNl('groovy> ', promptStyle)
                appendOutput(line, commandStyle)
            }
            appendOutputNl(" \n", promptStyle)
        }

        // Kick off a new thread to do the evaluation
        // Run in a thread outside of EDT, this method is usually called inside the EDT
        try {
            SwingUtilities.invokeLater { showExecutingMessage() }
            String name = scriptFile?.name ?: (DEFAULT_SCRIPT_NAME_START + scriptNameCounter++)
            LOGGER.info " !!!! HERE !!!!"
            newScript()
            def result = shell.evaluate(record.getTextToRun(false), name)
            SwingUtilities.invokeLater { finishNormal(result) }
        } catch (Throwable t) {
            if (t instanceof StackOverflowError) {
                // set the flag that will be used in printing exception details in output pane
                stackOverFlowError = true
                clearOutput()
            }
            SwingUtilities.invokeLater { finishException(t, true) }
        } finally {
            scriptRunning = false
            interruptAction.enabled = false
        }
    }
}