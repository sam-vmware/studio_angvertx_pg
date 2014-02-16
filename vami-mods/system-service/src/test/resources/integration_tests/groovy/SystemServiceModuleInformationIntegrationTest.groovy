package integration_tests.groovy

import com.vmware.studio.vamimods.system.SystemService
import org.vertx.groovy.testtools.VertxTests

import static org.vertx.testtools.VertxAssert.*

/**
 * Created by samueldoyle on 2/13/14.
 * Operational tests
 */

def testSystemServiceSystemInformationUnknownOperation() {
    def getSystemInfoMsg = [
        type     : "SystemInformation",
        operation: "fooBlah"
    ]
    vertx.eventBus.send(SystemService.MY_ADDRESS, getSystemInfoMsg, { reply ->
        container.logger.info "body: ${reply.body}"
        assertEquals(reply.body.result, "error")
        testComplete()
    })
}

def testSystemServiceSystemInformation() {
    def getSystemInfoMsg = [
        type     : "SystemInformation",
        operation: "testGetSystemInformation"
    ]
    vertx.eventBus.send(SystemService.MY_ADDRESS, getSystemInfoMsg, { reply ->
        container.logger.info "body: ${reply.body}"
        assertEquals(reply.body.result, "ok")
        assertEquals(reply.body.data.product as String, "VMware Studio")
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