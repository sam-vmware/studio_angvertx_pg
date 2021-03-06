package com.vmware.studio.shared.utils

import groovy.transform.CompileStatic

/**
 * Created by samueldoyle on 2/13/14.
 *
 * Note this is using ThreadLocal if you go and deploy another verticle from within a verticle these
 * values aren't preserved unless you configure the classloaders in such a way
 */
@CompileStatic
@Singleton(lazy = true)
class ResourceLoader {

    class ConfigContainer {
        ConfigObject configObject
        HashMap<String, String> configMap

        public ConfigContainer(ConfigObject configObject, HashMap<String, String> configMap) {
            this.configMap = configMap
            this.configObject = configObject
        }
    }

    private static InheritableThreadLocal<ConfigContainer> CONFIG =
        new InheritableThreadLocal<ConfigContainer>()

    private void setTheConfig(ConfigObject co) {
        HashMap<String, String> configMap = co.flatten() as HashMap<String, String>
        def cc = new ConfigContainer(co, configMap)
        CONFIG.set(cc)
    }

    /**
     * Loads config into thread local
     * @ URL contains the target file or resource
     * @return
     */
    public void loadConfigObject(URL configURL, String environment = "dev") {
        ConfigObject co = new ConfigSlurper(environment).parse(configURL);
        setTheConfig(co)
    }

    /**
     * If in a jar or somewhere where a groovy script has been compiled for exampled can be loaded like this
     * @param fqClassname the fully qualified classname e.g. "com.vmware.com.SomeClass"
     */
    public void loadConfigObject(String fqClassname,
                                 ClassLoader targetLoader = ResourceLoader.instance.class.classLoader, String environment = "dev") {
        Class scriptClass = targetLoader.loadClass(fqClassname)
        ConfigObject co = new ConfigSlurper(environment).parse(scriptClass)
        setTheConfig(co)
    }

    /**
     * Just some config script to set
     * @param configObject
     */
    public void loadConfigObject(Script configScript, String environment = "dev") {
        ConfigObject co = new ConfigSlurper(environment).parse(configScript)
        setTheConfig(co)
    }

    /**
     * First check config object, if not there then try direct
     * @param property
     * @return
     */
    public String getConfigProperty(String property) {
        return CONFIG.get().configMap[property]
    }

    /**
     * Set on this delegates to the threadlocal config
     * @param property
     * @param newValue
     */
    public void setConfigPropery(String propertyName, Object newValue) {
        CONFIG.get().configMap[propertyName] = newValue
    }

    /**
     * Just get the configobject like for using javaobjects
     * @return
     */
    public ConfigObject getConfigObject() {
        return CONFIG.get().configObject
    }
}
