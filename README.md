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

## For local cluster running ##
# 1. Disable multicast and enable tcp-ip and set tcp-ip interface to 127.0.0.1
$ vi $VERTX_HOME/conf/cluster.xml
# Change sections to look like this:
#            <multicast enabled="false"></multicast>
#            <tcp-ip enabled="true">
#                <interface>127.0.0.1</interface>
#            </tcp-ip>
# 2. Change mod-lang-groovy version
$ vi $VERTX_HOME/conf/langs.properties
# Use this value
# Keep this in sync with whatever the version is of Vert.x. Once you have built Vert.x you can get a copy from
# vertx/build/<vert.x-XXXXX>/conf
# 3. Change repos.txt and remove local repo
# avoid picking up any of your stale builds
$ vi $VERTX_HOME/conf/repos.txt
# comment out the local
#mavenLocal:~/.m2/repository
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
$ ./gradlew vami-mods:web-server:test -i
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
# First need to use vertx module-link to link our local repo source modules to be used by vertx
# If you have done this a few times you may want to remove an existing link if it is already there
# vertx doesn't provide a way to relink
$ cd vami-mods/web-server
$ vertx create-module-link com.vmware~vami-web-server~1.0
$ cd vami-mods/content-resolver
$ vertx create-module-link com.vmware~vami-content-resolver~1.0
$ cd vami-mods/system-service
$ vertx create-module-link com.vmware~vami-system-service~1.0
# There is a modlink script in the root directory that might be good for you

# Now we can run the mods
$ cd $VERTX_MODS
# terminal 1, vami-web-server
$ vertx runmod com.vmware~vami-web-server~1.0 -cluster -cluster-host 127.0.0.1 -cluster-port 9000
# terminal 2, vami-content-resolver
$ vertx runmod com.vmware~vami-content-resolver~1.0 -cluster -cluster-host 127.0.0.1 -cluster-port 9001
# terminal 3, vami-system-service
$ vertx runmod com.vmware~vami-system-service~1.0 -cluster -cluster-host 127.0.0.1 -cluster-port 9002
```

*NOTE: It is possible to run a [Verticle](http://vertx.io/manual.html#verticle) in your IDE, see Developing [Vert.x Modules with Gradle](http://vertx.io/gradle_dev.html)*


#### Manual Test
If all went well you should see the main page at: [localhost:8080](localhost:8080)

Authentication is **PAM** based so assuming you are running on Linux you should be able to login using a valid user
that can be authenticated through ssh.

#### Scaffolding
To quickly generate a scaffolded mod which produces a working sample application and structure use the createApp task
```bash
./gradlew createMod
```
A swing dialog is presented which you enter two fields a modowner and modname
* **modowner** is like a Maven group (com. is automatically prefixed) e.g. **foo.bar** would produce **com.foo.bar**
* **modname** is the name of the module e.g. **pop** 
* **mainTabName** will be the title of the your modules initial tab when the service is loaded
* **helloMessageField** is the message returned from your Vert.x module in response to the request sent on page load

*For more on Vert.x Module Naming Convention see: http://vertx.io/mods_manual.html*

The scaffolded module will be output to: scaffolding/generated/scaffold-(your modname)
##### Building
To build the scaffold modify the settings.gradle to include this as a project
```bash
include 'shared:common'
include 'vami-mods', 'vami-mods:system-service', 'vami-mods:content-resolver', 'vami-mods:web-server'

// example for a scaffolded app. In this case input for modname was pop
//include 'scaffolding:generated:scaffold-pop'
```
Now **./gradlew assemble** will pick this up and generate it as a Vert.x mod.
##### Running the Scaffold
This is the same as any of the other modules. You would repeat the same steps for this module as was described in the previous Running section for example if you named the mod pop:

```bash
$ cd scaffolding/generated/scaffold-pop
$ vertx create-module-link vertx create-module-link com.foo.bar~pop~1.0
$ cd $VERTX_MODS/com.foo.bar~pop~1.0
$ ln -s $SRC_ROOT/scaffolding/generated/scaffold-pop/web-ui/app
$ vertx runmod com.foo.bar~pop~1.0 -cluster -cluster-host 127.0.0.1 -cluster-port 9002
```
Once again take a look at the **modlink** script in the project root directory to see an example
