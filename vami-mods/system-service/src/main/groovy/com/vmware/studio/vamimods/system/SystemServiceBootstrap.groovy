package com.vmware.studio.vamimods.system

import org.vertx.groovy.platform.Verticle

class SystemServiceBootstrap extends Verticle {
    def start() {
        container.logger.info "Hello from ${this.class.name}"
    }
}
