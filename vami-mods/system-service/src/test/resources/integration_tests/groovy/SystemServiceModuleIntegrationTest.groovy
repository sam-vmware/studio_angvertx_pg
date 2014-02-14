package integration_tests.groovy
import static org.vertx.testtools.VertxAssert.*
import org.vertx.groovy.testtools.VertxTests

import com.vmware.studio.vamimods.system.SystemService

/**
 * Created by samueldoyle on 2/13/14.
 */

def testInvalidMsgBody() {
    container.logger.info("Running testInvalidMsgBody")
    def msg = "Hi I'm a String"
    vertx.eventBus.send(SystemService.MY_ADDRESS, msg, { reply ->
        assertEquals(reply.body.result, "error")
        assert reply.body.cause.startsWith("Invalid message body payload type received")
        testComplete()
    })
}

def testInvalidMsgMissingOperation() {
    container.logger.info("Running testSystemServiceUnknownMessage")
    def missingOperationType = [
            type: "TimeZone",
    ]
    vertx.eventBus.send(SystemService.MY_ADDRESS, missingOperationType, { reply ->
        assertEquals(reply.body.result, "error")
        assert reply.body.cause.startsWith("Message validation failed")
        testComplete()
    })
}

def testInvalidMsgUnknownType() {
    container.logger.info("Running testSystemServiceUnknownMessage")
    def unknownMsgType = [
        type: "foo",
        operation: "get"
    ]
    vertx.eventBus.send(SystemService.MY_ADDRESS, unknownMsgType, { reply ->
        assertEquals(reply.body.result, "error")
        assert reply.body.cause.startsWith("Unknown message type received")
        testComplete()
    })
}

def testInvalidMsgWrongOperation() {
    container.logger.info("Running testSystemServiceUnknownMessage")
    def missingOperationType = [
            type: "TimeZone",
            operation: "foo"
    ]
    vertx.eventBus.send(SystemService.MY_ADDRESS, missingOperationType, { reply ->
        assertEquals(reply.body.result, "error")
        assert reply.body.cause.startsWith("Unknown operation type received")
        testComplete()
    })
}

def testSystemServiceTZMessage() {
    container.logger.info("Running testSystemServiceUnknownMessage")
    def unknownMsgType = [
        type: "TimeZone",
        operation: "get"
    ]
    vertx.eventBus.send(SystemService.MY_ADDRESS, unknownMsgType, { reply ->
        assertEquals(reply.body.result, "ok")
        println "Returned TimeZone: ${reply.body.data}"
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