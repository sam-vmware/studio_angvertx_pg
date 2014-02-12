package com.vmware.studio.vamimods.system

import org.vertx.groovy.platform.Verticle

class SystemService extends Verticle {

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
            //host              : 'localhost',
            host              : '0.0.0.0',
            port              : 8080,
            web_root: '/home/samueldoyle/Projects/VMware/StudioRepo/git/NewStack/studio_angular_vertx/web-ui/app',
            index_page: 'index.html',
            static_files      : true,
            ssl               : false,
            bridge            : true,
            inbound_permitted : [[:]],
            outbound_permitted: [[:]]
    ]

    def start() {
        container.logger.info  "!!!! Hello Again From SystemService !!!!"
    }
}