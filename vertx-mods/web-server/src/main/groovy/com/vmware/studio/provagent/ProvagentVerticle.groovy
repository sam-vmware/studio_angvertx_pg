package com.vmware.studio.provagent

import org.vertx.groovy.platform.Verticle

import static org.vertx.groovy.core.streams.Pump.createPump

/**
 * Created by samueldoyle on 2/10/14.
 */
class ProvagentVerticle extends Verticle {
    def start() {
        vertx.createNetServer().connectHandler { socket ->
            createPump(socket, socket).start()
        }.listen(1234)
    }
}
