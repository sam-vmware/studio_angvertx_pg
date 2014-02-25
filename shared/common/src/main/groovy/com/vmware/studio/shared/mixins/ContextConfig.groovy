package com.vmware.studio.shared.mixins

import com.vmware.studio.shared.utils.CurrentContext
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.platform.Container

/**
 * Created by samueldoyle
 * Wrap the context handling in something we can mixin
 */
class ContextConfig {
    def SET_VERTX(vertx) {
        CurrentContext.instance.vertxInstance = vertx as Vertx
        this
    }

    def GET_VERTX() {
        CurrentContext.instance.vertxInstance
    }

    def SET_CONTAINER(container) {
        CurrentContext.instance.containerInstance = container as Container
        this
    }

    def GET_CONTAINER() {
        CurrentContext.instance.containerInstance
    }

    def DUMP_CONTAINER_ENV() {
        def container = GET_CONTAINER()
        container.logger.info "========== CONTAINER ENV DUMP BEGIN =========="
        container.env.each { key, value ->
            container.logger.info "$key --> $value"
        }
        container.logger.info "========== CONTAINER ENV DUMP END =========="
        this
    }

    def DUMP_CONTAINER_CONF() {
        def container = GET_CONTAINER()
        container.logger.info "========== CONTAINER CONFIG DUMP BEGIN =========="
        container.config.each { key, value ->
            container.logger.info "$key --> $value"
        }
        container.logger.info "========== CONTAINER CONFIG DUMP END =========="
        this
    }

    /**
     * Returns the working directory as seen by the module
     * This can vary, see the documentation on the
     * "preserve-cwd" property set in mod.json
     * @return
     */
    def GET_BASE_ROOT() {
        System.getProperty("user.dir")
    }

    /**
     * Get the VERTX_MODS directory. This is the env setting where the vertx mods are placed
     * @return
     */
    def GET_MOD_DIR() {
        GET_CONTAINER().env["VERTX_MODS"]
    }
}
