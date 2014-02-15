package integration_tests.groovy
import static org.vertx.testtools.VertxAssert.*
import org.vertx.groovy.testtools.VertxTests

import com.vmware.studio.vamimods.system.SystemService

/**
 * Created by samueldoyle on 2/13/14.
 * Operational tests
 */

def testSystemServiceTZMessage() {
    container.logger.info("Running testSystemServiceTZMessage")
    def getTZMsg = [
        type: "TimeZone",
        operation: "get"
    ]
    vertx.eventBus.send(SystemService.MY_ADDRESS, getTZMsg, { reply ->
        println "body: ${reply.body}"
        assertEquals(reply.body.result, "ok")
        testComplete()
    })
}

def testSetTZ() {
    container.logger.info("Running testSetTZ")
    def setTZMsg = [
            type: "TimeZone",
            operation: "testSetTimeZone",
            data: [
                    newTimeZone: "Arctic/Longyearbyen"
                  ]
    ]
    vertx.eventBus.send(SystemService.MY_ADDRESS, setTZMsg, { reply ->
        println "body: ${reply.body}"
        assertEquals(reply.body.result, "ok")
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