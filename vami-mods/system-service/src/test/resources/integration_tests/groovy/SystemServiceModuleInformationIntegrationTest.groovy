package integration_tests.groovy

import com.vmware.studio.shared.utils.GlobalServiceConfig
import com.vmware.studio.vamimods.system.SystemService
import org.vertx.groovy.testtools.VertxTests

import static org.vertx.testtools.VertxAssert.*

/**
 * Created by samueldoyle on 2/13/14.
 * Operational tests
 */

serviceAddress = GlobalServiceConfig.instance.systemServiceCommonConfig.service.address
def testSystemServiceSystemInformationUnknownOperation() {
    def getSystemInfoMsg = [
        type     : "SystemInformation",
        operation: "fooBlah"
    ]
    vertx.eventBus.send(serviceAddress, getSystemInfoMsg, { reply ->
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
    vertx.eventBus.send(serviceAddress, getSystemInfoMsg, { reply ->
        container.logger.info "body: ${reply.body}"
        assertEquals(reply.body.result, "ok")

        def productEntry = reply.body.data.find {
            it.key == "product"
        }
        assert productEntry
        assertEquals(productEntry.value as String, "VMware Studio")
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