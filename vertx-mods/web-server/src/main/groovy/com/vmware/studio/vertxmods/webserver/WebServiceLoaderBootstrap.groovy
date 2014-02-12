package com.vmware.studio.vertxmods.webserver

import org.vertx.groovy.platform.Verticle

class WebServiceLoaderBootstrap extends Verticle {

    def start() {
        def eb = vertx.eventBus
        def pa = "vertx.mongopersistor"
        container.logger.info "${this.class.name}: Sending db updates ..."
        eb.send(pa, [action: "delete", collection: "users", matcher: [:]]) {
            eb.send(pa, [
                    action    : "save",
                    collection: "users",
                    document  : [
                            firstname: "Sam",
                            lastname : "Doyle",
                            email    : "samuledoyle@lvmware.com",
                            username : "sam",
                            password : "password"
                    ]
            ]) {
                container.logger.info "${this.class.name}: DB updates completed ..."
            }
        }
    }
}
