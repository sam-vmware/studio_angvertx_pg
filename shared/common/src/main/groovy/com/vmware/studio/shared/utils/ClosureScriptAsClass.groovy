package com.vmware.studio.shared.utils

/**
 * Created by samueldoyle on 2/13/14.
 * can take a closure which will be invoked in place of this
 * i.e. new ClosureScriptAsClass(closure: <some closure>)
 */
class ClosureScriptAsClass extends Script {
    Closure closure

    def run() {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = this
        closure.call()
    }
}
