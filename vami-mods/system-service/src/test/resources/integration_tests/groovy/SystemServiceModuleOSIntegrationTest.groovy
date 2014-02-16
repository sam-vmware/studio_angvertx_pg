package integration_tests.groovy

import com.vmware.studio.vamimods.system.SystemService
import org.vertx.groovy.testtools.VertxTests

import static org.vertx.testtools.VertxAssert.*

/**
 * Created by samueldoyle on 2/13/14.
 * Operational tests
 */

def testSystemServiceDoReboot() {
    def doRebootMsg = [
        type     : "OperatingSystem",
        operation: "testDoReboot"
    ]
    vertx.eventBus.send(SystemService.MY_ADDRESS, doRebootMsg, { reply ->
        println "body: ${reply.body}"
        assertEquals(reply.body.result, "ok")
        assertEquals(reply.body.data as String, "/sbin/shutdown -r -t 5 now")
        testComplete()
    })
}

def testSystemServiceDoShutdown() {
    def doRebootMsg = [
        type     : "OperatingSystem",
        operation: "testDoShutdown"
    ]
    vertx.eventBus.send(SystemService.MY_ADDRESS, doRebootMsg, { reply ->
        println "body: ${reply.body}"
        assertEquals(reply.body.result, "ok")
        assertEquals(reply.body.data as String, "/sbin/shutdown -h -t 5 now")
        testComplete()
    })
}

VertxTests.initialize(this)

container.deployModule(System.getProperty("vertx.modulename"), { asyncResult ->
    // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
    assertTrue(asyncResult.succeeded)
    assertNotNull("deploymentID should not be null", asyncResult.result())
    // If deployed correctly then start the tests!
    VertxTests.startTests(this)
})