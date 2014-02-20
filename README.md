studio_angvertx_pg
==================
#### Building
``` bash
# First change any direct paths in the main build.gradle file to be what you need to be.
$ vi ./build.gradle
# Change anything in the variable block: ext.VMWARE_CONF
# groovyConsolePath - only matters if you want to play around with the console, this sets up the classpath for the project so the console will allow you play around testing things
# libDir - this needs to be the lib directory of your Vert.X build if you have done a custom build from the GitHub checkout: https://github.com/eclipse/vert.x.git
# There is probably a better Gradle way of doing this as in properties or something. TBD

# Change the WebServiceLoader web_root configure property (atm it is hardcoded, external properties are passed to Vert.x modules via a conf file) to the absolute path of the web app directory.
$ vi ./vertx-mods/web-server/src/main/groovy/com/vmware/studio/vertxmods/webserver/WebServiceLoader.groovy
# Change this line
# web_root          : '/opt/vmware/share/vertx_extra/web_root/web_app'
# To be the directory where your local version is which should be some like: 
# /home/me/myprojects/studio_angvertx_pg/web-ui/app

# Build
$ ./gradlew assemble
```
For **clean**
```bash
$ ./gradlew clean
```
#### Testing
Tests are integration at the moment and per module, since we use Gradle. You run tests as in a normal Gradle fashion.
``` bash
$ ./gradlew vami-mods:system-service:test
$ ./gradlew vertx-mods:web-server:test
```
Build results found in each of the modules directories.
```bash
ls -1 ./vami-mods/system-service/build/reports/tests
classes
css
htc
index.html
js
packages
```
#### Running
There is a simple wrapper verticle called [App.groovy](https://github.com/sam-vmware/studio_angvertx_pg/blob/master/App.groovy) that calls the Vert.x Container's *deployModule* that deploys each [Module](http://vertx.io/manual.html#module) in the same VM, otherwise for each module to communicate each would need to run in cluster mode. First however, you need to setup your workspace using the Vert.x module link for each module
```bash
$ cd vami-mods/system-service
# I have something not set right in the gradle build that causes the common.jar not to be placed in the build directory so this step is needed for now.
$ echo build/mods/com.vmware~vami-system-service~1.0/lib/common-1.0.jar >> vertx_classpath.txt
$ vertx create-module-link com.vmware~vami-system-service~1.0Attempting to create module link for module com.vmware~vami-system-service~1.0 
Succeeded in creating module link 

$ cd vertx-mods/web-server
$ vertx create-module-link com.vmware~vami-web-server~1.0
Attempting to create module link for module com.vmware~vami-web-server~1.0 
Succeeded in creating module link 

# Now run the wrapper verticle
$ vertx run App.groovy
$ vertx run App.groovy 
Main main.App Starting 
Deploying vami-services ... 
Deploying web-server ... 
Succeeded in deploying verticle 
com.vmware.studio.vertxmods.webserver.WebServiceLoader Deployment succeeded for: com.vmware.studio.vertxmods.webserver.WebServiceLoader 
com.vmware.studio.vamimods.system.SystemService Deployment succeeded for: com.vmware.studio.vamimods.system.SystemService 
com.vmware.studio.vertxmods.webserver.WebServiceLoader Server Listening on port: 8080, host: 0.0.0.0 
Registering enabled service: TimeZoneMessageHandler 
Registering enabled service: InformationMessageHandler 
Registering enabled service: OperatingSystemHelper 
Registering local service address handler @ vami.SystemService 
```

*NOTE: It is possible to run a [Verticle](http://vertx.io/manual.html#verticle) in your IDE, see Developing [Vert.x Modules with Gradle](http://vertx.io/gradle_dev.html)*

If all went well you should see the main page at: [localhost:8080](localhost:8080)
