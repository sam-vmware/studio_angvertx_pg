package integration_tests.groovy

import com.vmware.studio.shared.utils.GlobalServiceConfig
import org.vertx.groovy.testtools.VertxTests

import static org.vertx.testtools.VertxAssert.*

/**
 * Created by samueldoyle on 2/13/14.
 * Operational tests
 */

serviceAddress = GlobalServiceConfig.instance.systemServiceCommonConfig.service.address
def testSystemServiceTZMessage() {
    def getTZMsg = [
        type     : "TimeZone",
        operation: "get"
    ]
    vertx.eventBus.send(serviceAddress, getTZMsg, { reply ->
        println "body: ${reply.body}"
        assertEquals(reply.body.result, "ok")
        testComplete()
    })
}

def testSetTZ() {
    def setTZMsg = [
        type     : "TimeZone",
        operation: "testSetTimeZone",
        data     : [
            newTimeZone: "Arctic/Longyearbyen"
        ]
    ]
    vertx.eventBus.send(serviceAddress, setTZMsg, { reply ->
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