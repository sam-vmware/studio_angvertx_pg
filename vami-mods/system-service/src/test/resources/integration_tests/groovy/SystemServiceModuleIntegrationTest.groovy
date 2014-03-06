package integration_tests.groovy

import com.vmware.studio.shared.utils.GlobalServiceConfig
import com.vmware.studio.vamimods.system.SystemService
import org.vertx.groovy.testtools.VertxTests

import static org.vertx.testtools.VertxAssert.*

/**
 * Created by samueldoyle on 2/13/14.
 */

serviceAddress = GlobalServiceConfig.instance.systemServiceCommonConfig.service.address
def testInvalidMsgBody() {
    def msg = "Hi I'm a String"
    vertx.eventBus.send(serviceAddress, msg, { reply ->
        assertEquals(reply.body.result, "error")
        assert reply.body.cause.startsWith("Invalid message body payload type received")
        testComplete()
    })
}

def testInvalidMsgMissingOperation() {
    def missingOperationType = [
        type: "TimeZone"
    ]
    vertx.eventBus.send(serviceAddress, missingOperationType, { reply ->
        assertEquals(reply.body.result, "error")
        assert reply.body.cause.startsWith("Message validation failed")
        testComplete()
    })
}

def testInvalidMsgUnknownType() {
    def unknownMsgType = [
        type     : "foo",
        operation: "get"
    ]
    vertx.eventBus.send(serviceAddress, unknownMsgType, { reply ->
        assertEquals(reply.body.result, "error")
        assert reply.body.cause.startsWith("Unknown message type received")
        testComplete()
    })
}

def testInvalidMsgWrongOperation() {
    def missingOperationType = [
        type     : "TimeZone",
        operation: "foo"
    ]
    vertx.eventBus.send(serviceAddress, missingOperationType, { reply ->
        assertEquals(reply.body.result, "error")
        assert reply.body.cause.startsWith("Unknown operation type received")
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