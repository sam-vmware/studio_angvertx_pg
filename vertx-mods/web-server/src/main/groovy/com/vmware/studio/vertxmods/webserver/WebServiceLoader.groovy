/**
 * Created by samueldoyle
 */
package com.vmware.studio.vertxmods.webserver

import org.vertx.groovy.core.Vertx
import org.vertx.groovy.platform.Verticle
import org.vertx.groovy.core.http.RouteMatcher

class WebServiceLoader extends Verticle {
    private final ME = this.class.name

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
            web_root          : '/home/samueldoyle/Projects/VMware/StudioRepo/git/NewStack/studio_angular_vertx/web-ui/app',
            //web_root          : '/opt/vmware/share/vertx_extra/web_root/web_app',
            index_page        : 'index.html',
            static_files      : true,
            ssl               : false,
            bridge            : true,
            inbound_permitted : [[:]],
            outbound_permitted: [[:]]
    ]

    def DEFAULT_PORT = 8080;
    def DEFAULT_ADDRESS = "0.0.0.0";
    def DEFAULT_WEB_ROOT = "web";
    def DEFAULT_INDEX_PAGE = "index.html";
//    def DEFAULT_AUTH_ADDRESS = "vertx.basicauthmanager.authorise";
    def  DEFAULT_AUTH_TIMEOUT = 5 * 60 * 1000;

    def start() {
        container.logger.info "$ME Deployment succeeded for: ${this.class.name}"

/*        container.deployModule('io.vertx~mod-mongo-persistor~2.0.0-final', mongoPersistorConf) { asyncResult ->
            if (asyncResult.succeeded) {
                // Bootstrap some data
                container.deployVerticle("groovy:com.vmware.studio.vertxmods.webserver.WebServiceLoaderBootstrap")
            } else {
                println "Failed to deploy ${asyncResult}"
            }
        }
        // Start the web server, with the config we defined above
        container.deployModule('io.vertx~mod-auth-mgr~2.0.0-final', authManagerConf)

        // Start the web server, with the config we defined above
        container.deployModule('io.vertx~mod-web-server~2.0.0-final', webServerConf)*/

/*        def webRoot = container.config["web_root"] ?: DEFAULT_WEB_ROOT;
        def staticHandler = {
            def webRoot = webServerConf["web_root"]
            def index = webServerConf["index_page"]
            String webRootPrefix = "webRoot + ${File.separator}"
            String indexPage = "$webRootPrefix$index"
            boolean gzipFiles = webServerConf["gzip_files"] ?: false
            boolean caching = webServerConf["caching"] ?: false

            new StaticFileHandler(vertx, webRootPrefix, indexPage, gzipFiles, caching);
        }
        def routeMatcher = {
            def matcher = new RouteMatcher();
            matcher.noMatch(staticHandler);
            matcher;
        }

        def server = vertx.createHttpServer()

        if (webServerConf["ssl"]) {
            server.setSSL(true).setKeyStorePassword((webServerConf["key_store_password"] ?: "wibble") as String)
                .setKeyStorePath((webServerConf["key_store_path"] ?: "server-keystore.jks") as String);
        }

        if (webServerConf["route_matcher"]) {
            server.requestHandler(routeMatcher)
        } else  if (webServerConf["static_files"]) {
            server.requestHandler(staticHandler);
        }

        if (webServerConf["bridge"]) {
            def inboundPermitted = webServerConf["inbound_permitted"] ?: [[:]]
            def outboundPermitted = webServerConf["outbound_permitted"] ?: [[:]]
            vertx.createSockJSServer(server).bridge(prefix: '/eventbus', inboundPermitted, outboundPermitted)
        }
        server.requestHandler(staticHandler);*/

        // Serve the static resources
        def host = webServerConf["host"]
        def port = webServerConf["port"]
        def webRoot = webServerConf["web_root"]
        def server = vertx.createHttpServer().requestHandler { req ->
            def file = req.uri == "/" ? "index.html" : req.uri
            req.response.sendFile "$webRoot/$file"
        }

        vertx.createSockJSServer(server).bridge(prefix: '/eventbus', [[:]], [[:]])

        container.logger.info "$ME Server Listening on port: $port, host: $host"
        server.listen(port,host)
    }
}
