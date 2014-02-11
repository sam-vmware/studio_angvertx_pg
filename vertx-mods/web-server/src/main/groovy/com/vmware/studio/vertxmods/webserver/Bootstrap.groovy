package com.vmware.studio.vertxmods.webserver
import org.vertx.groovy.platform.Verticle

class Bootstrap extends Verticle {

    def eb = vertx.eventBus
    def pa = "vertx.mongopersistor"

    def start() {
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
            ])
        }
    }
}
