package com.vmware.studio.vamimods.contentresolver

import groovy.util.logging.Log
import org.vertx.groovy.platform.Verticle

import static org.vertx.testtools.VertxAssert.assertEquals
import static org.vertx.testtools.VertxAssert.testComplete

/**
 * Created by samueldoyle on 2/13/14.
 */
@Log(value = "LOGGER")
class ContentResolverServiceTest extends Verticle {
    def start() {
        assertEquals("true", "true");
        LOGGER.info "Inside ContentResolverServiceTest"
        testComplete();
    }
}
