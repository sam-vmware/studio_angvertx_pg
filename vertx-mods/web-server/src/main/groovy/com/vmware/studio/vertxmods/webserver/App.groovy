package com.vmware.studio.vertxmods.webserver

import org.vertx.groovy.platform.Verticle

class App extends Verticle {

    def authManagerConf = [
            // addres: 'vertx.basicauthmanager',
            // persistor_address: 'vertx.mongopersistor',
            user_collection: 'users',
            session_timeout: 1800000 // 30mins
    ]

    def mongoPersistorConf = [
            host   : 'localhost',
            port   : 27017,
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

    def start() {
        container.with {
            deployModule('io.vertx~mod-mongo-persistor~2.0.0-final', mongoPersistorConf) { asyncResult ->
                if (asyncResult.succeeded) {
                    // Bootstrap some data
                    deployVerticle('Bootstrap')
                } else {
                    println "Failed to deploy ${asyncResult}"
                }
            }

            // Start the web server, with the config we defined above
            deployModule('io.vertx~mod-auth-mgr~2.0.0-final', authManagerConf)

            // Start the web server, with the config we defined above
            deployModule('io.vertx~mod-web-server~2.0.0-final', webServerConf)
        }
    }
}