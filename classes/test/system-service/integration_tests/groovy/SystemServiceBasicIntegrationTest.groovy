package integration_tests.groovy

import com.vmware.studio.vamimods.system.test.integration.groovy.SystemServiceTest

import static org.vertx.testtools.VertxAssert.*

// And import static the VertxTests script
import org.vertx.groovy.testtools.VertxTests

/**
 * Created by samueldoyle on 2/13/14.
 */

def testDeployArbitraryVerticle() {
    assertEquals("true", "true")
    container.deployVerticle("groovy:" + SystemServiceTest.class.getName())
}

VertxTests.initialize(this)
VertxTests.startTests(this)