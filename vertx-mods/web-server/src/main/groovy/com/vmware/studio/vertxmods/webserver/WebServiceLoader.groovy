/**
 * Created by samueldoyle
 */
package com.vmware.studio.vertxmods.webserver

import com.vmware.studio.shared.categories.CommonService
import com.vmware.studio.shared.mixins.ContextConfig
import com.vmware.studio.shared.mixins.ResourceEnabled
import com.vmware.studio.shared.services.Service
import com.vmware.studio.shared.utils.ClosureScriptAsClass
import com.vmware.studio.shared.utils.GlobalServiceConfig
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.platform.Verticle

@Mixin([ResourceEnabled, ContextConfig, CommonService])
class WebServiceLoader extends Verticle implements Service {
    final String CURRENT_ENVIRONMENT = System.properties["env"] ?: "dev"

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
        web_root          : '', // fill this in or defaults to the mod directory (which it should probably be)
        // web_root          : '/home/samueldoyle/Projects/VMware/StudioRepo/git/NewStack/studio_angular_vertx/web-ui/app',
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
    def DEFAULT_AUTH_TIMEOUT = 5 * 60 * 1000;

    def myConfigObject = {
        services {
            newServiceAnnounceChannel = "vami.newServiceAnnounceChannel"
            webService {
                // This is the directory we expect to find be available from the start directory
                // which should be the VERTX_MODS directory e.g.
                // MY_ROOT_SHOULD_BE_HERE = $VERTX_MODS/$expectedRootMatch
                expectedRootMatch = "com.vmware~vami-web-server~1.0"
            }
        }
        environments {
            dev {
            }
            test {
            }
            prod {
            }
        }
    }


    def postInitialize() {
    }

    /*********************************** OVERRIDES BELOW ***********************************/

    def routeMatcher = new RouteMatcher()

    @Override
    def start() {
        container.logger.info "Deployment succeeded"
        container.logger.info "*** Environment: $CURRENT_ENVIRONMENT"

        // Load config
        loadLocalResource(new ClosureScriptAsClass(myConfigObject))

        // Share container and vertx
        SET_CONTAINER(container).SET_VERTX(vertx)

        /*
        DUMP_CONTAINER_CONF()
        DUMP_CONTAINER_ENV()
        */
        // CommonService Mixin on Service
        // Verify our install root is where it should be
        // TBD embed this in some config logic
        if (CURRENT_ENVIRONMENT != "test") {
            VERIFY_INSTALL_ROOT()
        }

        postInitialize()

        // Serve the static resources
        def host = webServerConf["host"]
        def port = webServerConf["port"]
        def baseRoot = webServerConf["web_root"] ?: GET_MOD_DIR()
        def myWebRoot = "${getExpectedRoot().name}/app"
        container.logger.with {
            info "========== WebServiceLoader Starting =========="
            info "host: $host"
            info "port: $port"
            info "base root: $baseRoot"
            info "relative root: $myWebRoot"
            info "==============================================="
        }

        def server = vertx.createHttpServer()
/*        def routeMatcher = new RouteMatcher()
        routeMatcher.get("/") { req ->
            req.response.sendFile("$baseRoot/$myWebRoot/index.html")
        }
        server.requestHandler(routeMatcher.asClosure())*/
        server.requestHandler { req ->
            def file
            switch (req.uri) {
                case "/":
                    file = "$baseRoot/$myWebRoot/index.html"
                    break
                case ~/^\/com\..+/:
                    file = "$baseRoot/${req.uri}"
                    break
                default:
                    file = "$baseRoot/$myWebRoot/${req.uri}"
            }
            req.response.sendFile file
        }

        vertx.createSockJSServer(server).bridge(prefix: '/eventbus', [[:]], [[:]])
        server.listen(port, host)
    }

    /*********************************** OVERRIDES BELOW ***********************************/

    @Override
    File getExpectedRoot() {
        new File(GET_CONFIG().services.webService.expectedRootMatch)
    }

    @Override
    void registerService() {
        // webserviceloader has no content
    }

    @Override
    void handleGlobalResourceMessage(Map message) {
        // webserviceloader has no content
    }

    @Override
    String getServiceName() {
        GlobalServiceConfig.instance.webServiceCommonConfig.service.name
    }

    @Override
    String getAddress() {
        GlobalServiceConfig.instance.webServiceCommonConfig.service.address
    }
}
