package com.vmware.studio.vamimods.system

def eb = vertx.eventBus
def pa = "vertx.mongopersistor"

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
