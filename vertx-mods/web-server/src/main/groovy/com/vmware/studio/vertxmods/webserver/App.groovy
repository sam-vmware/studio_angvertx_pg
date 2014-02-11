package com.vmware.studio.vertxmods.webserver

def authManagerConf = [
    // addres: 'vertx.basicauthmanager',
    // persistor_address: 'vertx.mongopersistor',
    user_collection: 'users',
    session_timeout: 1800000 // 30mins
]

def mongoPersistorConf = [
        host   : 'localhost',
        port   : 9999,
        db_name: 'studio_db'
]

def webServerConf = [
        port              : 8080,
        host              : 'localhost',
        static_files      : 'true',
        ssl               : false,
        bridge            : true,
        inbound_permitted : [[:]],
        outbound_permitted: [[:]]
]

container.with {
    deployModule('mongo-persistor', mongoPersistorConf) { asyncResult ->
        if (asyncResult.succeeded) {
            // Bootstrap some data
            deployVerticle('Bootstrap.groovy')
        } else {
            println "Failed to deploy ${asyncResult.cause}"
        }
    }

    // Start the web server, with the config we defined above
    deployModule('auth-mgr', authManagerConf)

    // Start the web server, with the config we defined above
    deployModule('web-server', webServerConf)
}