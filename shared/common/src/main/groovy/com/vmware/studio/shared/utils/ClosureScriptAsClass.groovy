package com.vmware.studio.shared.utils
/**
 * Created by samueldoyle on 2/13/14.
 * can take a closure which will be invoked in place of this
 * i.e. new ClosureScriptAsClass(closure: <some closure>)
 */
class ClosureScriptAsClass extends Script {
    private List<Closure> closures = []
    private GroovyClassLoader groovyLoader = new GroovyClassLoader(this.class.classLoader.parent)

    public ClosureScriptAsClass(String closure) {
        this([closure] as String[])
    }

    public ClosureScriptAsClass(String[] closures) {
        for (strclosure in closures) {
            this.closures += evaluate(strclosure) as Closure
        }
    }

    public ClosureScriptAsClass(Closure closure) {
        this([closure] as Closure[])
    }

    public ClosureScriptAsClass(Closure[] closures) {
        this.closures.addAll(closures)
    }

    def run() {
        for (closure in this.closures) {
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.delegate = this
            closure.call()
        }
    }
}
