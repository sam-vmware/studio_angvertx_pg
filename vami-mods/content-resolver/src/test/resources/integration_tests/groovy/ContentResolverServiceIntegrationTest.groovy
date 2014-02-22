package integration_tests.groovy

import com.vmware.studio.vamimods.contentresolver.ContentResolverService
import org.vertx.groovy.testtools.VertxTests

import static org.vertx.testtools.VertxAssert.*

/**
 * Created by samueldoyle on 2/13/14.
 * Operational tests
 */

def testSystemContentResolverServiceInformationUnknownOperation() {
    def wrongOperationMsg = [
        type     : "ResourceRequestHandler",
        operation: "fooBlah"
    ]
    vertx.eventBus.send(ContentResolverService.MY_ADDRESS, wrongOperationMsg, { reply ->
        container.logger.info "body: ${reply.body}"
        assertEquals(reply.body.result, "error")
        testComplete()
    })
}

def testAddNewServiceFailedMissingKey() {
    def missingKeyMsg = [
        type     : "ResourceRequestHandler",
        operation: "addNewService",
        data     : [
            svcName: "testService"
        ]
    ]
    vertx.eventBus.send(ContentResolverService.MY_ADDRESS, missingKeyMsg, { reply ->
        container.logger.info "body: ${reply.body}"
        assertEquals(reply.body.result, "error")
        assert reply.body.cause.startsWith("New service validation failed, missing one or more requried keys")
        testComplete()
    })
}

def testAddNewServiceGood() {
    def goodMsg = [
        type     : "ResourceRequestHandler",
        operation: "addNewService",
        data     : [
            svcName: "testService",
            webRootDir: "/tmp"
        ]
    ]
    vertx.eventBus.send(ContentResolverService.MY_ADDRESS, goodMsg, { reply ->
        container.logger.info "body: ${reply.body}"
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
