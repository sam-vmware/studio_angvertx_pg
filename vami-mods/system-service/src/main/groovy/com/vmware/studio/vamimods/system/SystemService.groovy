package com.vmware.studio.vamimods.system

import org.vertx.groovy.platform.Verticle

class SystemService extends Verticle {
    def start() {
        container.deployVerticle("groovy:com.vmware.studio.vamimods.system.SystemServiceBootstrap") { asyncResult ->
            if (asyncResult.succeeded) {
                container.logger.info "Deployment succeeded for: ${this.class.name}"
            } else {
                println "Failed to deploy ${asyncResult.result()}"
            }
        }
    }
}