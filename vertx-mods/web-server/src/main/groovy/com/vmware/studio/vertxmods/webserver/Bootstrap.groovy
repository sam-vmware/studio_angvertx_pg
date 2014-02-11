package com.vmware.studio.vertxmods.webserver

def eb = vertx.eventBus
def pa = vertx.mongopersistor

// Delete users
eb.send(pa, [action: 'delete', collection: 'users', matcher: [:]]) {
    // Then add a user
    eb.send(pa, [
            action    : 'save',
            collection: 'users',
            document  : [
                    firstname: 'Sam',
                    lastname : 'Doyle',
                    email    : 'samuledoyle@lvmware.com',
                    username : 'sam',
                    password : 'ca$hc0w'
            ]
    ])
}
