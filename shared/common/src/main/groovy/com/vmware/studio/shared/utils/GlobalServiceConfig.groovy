package com.vmware.studio.shared.utils

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic

/**
 * Created by samueldoyle on 2/24/14.
 * Config getting too fragmented move common stuff here tht is service specific
 */
@Singleton(lazy = true)
@CompileStatic
class GlobalServiceConfig {

    def getWebServiceCommonConfig() {
        def builder = new JsonBuilder()
        builder.call([
            service: [
                name   : "WebServiceLoader",
                address: "vami.WebServiceLoader"
            ]
        ])
    }

    def getSystemServiceCommonConfig() {
        def builder = new JsonBuilder()
        builder.call([
            service: [
                web    : [
                    messages: [
                        resourceSvcRegistrationMsg: [
                            data     : [
                                title     : "System",
                                isDisabled: false,
                                svcName   : "vami.system",
                                webRootDir: "",
                                indexFile : "index.html"
                            ],
                            type     : "ResourceRequestHandler",
                            operation: "addNewService"
                        ]
                    ]
                ],
                name   : "SystemService",
                address: "vami.SystemService"
            ]
        ])
    }

    def getContentResolverServiceCommonConfig() {
        def builder = new JsonBuilder()
        builder.call([
            service: [
                name   : "ContentResolverService",
                address: "vami.ContentResolverService"
            ]
        ])
    }

    def getGlobalServiceCommonConfig() {
        def builder = new JsonBuilder()
        builder.call([
            service: [
                globalChannel: "vami.GLOBAL_RESOURCE_CHANNEL",
                messages     : [
                    reregister: [
                        type             : "REREGISTER_SERVICE",
                        originatorAddress: "vami.ContentResolverService"
                    ]
                ]
            ]
        ])
    }
}
