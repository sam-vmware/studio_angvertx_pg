studio_angvertx_pg
==================
#### Prerequisites
You need a local install of vert.x, I usually build source from GitHub and setup a link like this
```bash
$ git clone https://github.com/eclipse/vert.x.git vertx
$ cd vertx
$ ./gradlew distTar # Creates the distribution in tar format
$ cd build
$ ln -s vert.x-<VERSION>/ current
# Set a environment variable for your current link (.bashrc) e.g.
$ export VERTX_HOME="$HOME/Projects/GitHub/Vertx/vertx/build/current"
# Set the vert.x mod directory, this is where mods will be assumed and if downloaded placed
$ export VERTX_MODS="$HOME/Projects/GitHub/Vertx/mods"
$ mkdir -p $VERTX_MODS
```
#### Building
```bash
# First change any direct paths in the main build.gradle file to be what you need to be.
$ vi ./build.gradle
# Change anything in the variable block: ext.VMWARE_CONF
# groovyConsolePath - only matters if you want to play around with the console, this sets up the classpath for the project so the console will allow you play around testing things
# libDir - this needs to be the lib directory of your Vert.X build if you have done a custom build from the GitHub checkout: https://github.com/eclipse/vert.x.git

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
# Run all the tests
$ ./gradlew test
# Or ruch each modules individually
$ ./gradlew vertx-mods:web-server:test -i
$ ./gradlew vami-mods:content-resolver:test -i
$ ./gradlew vami-mods:system-service:test -i
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
There are three modules at the momement
1. vami-web-server
2. vami-content-resolver
3. vami-system-service

You probably want to run one in each terminal
```bash
$ cd $VERTX_MODS
# terminal 1, vami-web-server
$ vertx runmod com.vmware~vami-web-server~1.0 -cluster -cluster-host 127.0.0.1 -cluster-port 9000
# terminal 2, vami-content-resolver
$ vertx runmod com.vmware~vami-content-resolver~1.0 -cluster -cluster-host 127.0.0.1 -cluster-port 9001
# terminal 3, vami-system-service
$ vertx runmod com.vmware~vami-system-service~1.0 -cluster -cluster-host 127.0.0.1 -cluster-port 9002
```

*NOTE: It is possible to run a [Verticle](http://vertx.io/manual.html#verticle) in your IDE, see Developing [Vert.x Modules with Gradle](http://vertx.io/gradle_dev.html)*

If all went well you should see the main page at: [localhost:8080](localhost:8080)
