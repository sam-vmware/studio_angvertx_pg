package com.vmware.studio.shared.utils

import groovy.transform.CompileStatic
import groovy.util.logging.Log
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.platform.Container

/**
 * Created by samueldoyle on 2/23/14.
 * Store shared context variables such as container and vertx
 */
@CompileStatic
@Singleton(lazy = true)
@Log(value = "LOGGER")
class CurrentContext {
    public static final String VERTX = "vertx"
    public static final String CONTAINER = "container"
    // The VERTX_MODS is the env set to where the mod directory is located
    public static final String VERTX_MODS = "VERTX_MODS"
    private static InheritableThreadLocal ContextData =
        new InheritableThreadLocal() {
            @Override
            protected HashMap initialValue() {
                return new HashMap();
            }

        }

    public void setVertxInstance(Vertx vertx) {
        LOGGER.info("Setting new Vertx instance")
        ContextData.get()[VERTX] = vertx
    }

    public Vertx getVertxInstance() {
        ContextData.get()[VERTX] as Vertx
    }

    public void setContainerInstance(Container container) {
        LOGGER.info("Setting new Container instance")
        ContextData.get()[CONTAINER] = container
    }

    public Container getContainerInstance() {
        ContextData.get()[CONTAINER] as Container
    }
}
