package com.vmware.studio.vamimods.system.test.integration.groovy

import groovy.util.logging.Log
import static org.vertx.testtools.VertxAssert.*
import org.vertx.groovy.platform.Verticle

/**
 * Created by samueldoyle on 2/13/14.
 */
@Log(value = "LOGGER")
class SystemServiceTest extends Verticle {
    def start() {
        assertEquals("true", "true");
        LOGGER.info "Inside SystemServiceTestVerticle"
        testComplete();
    }
}
