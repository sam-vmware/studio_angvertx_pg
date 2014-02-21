'use strict';
/**
 * Created by samueldoyle
 */
vamiApp.lazy.controller('serviceTabsController', ['$q', '$log', '$scope', '$state', 'VAMI_ROOT',
    function ($q, $log, $scope, $state, VAMI_ROOT) {
        $scope.tabs = [
            { title: "VM", name: "vm", state: "serviceTabs.vm", active: false, disabled: false, templ: VAMI_ROOT + '/views/tabs/vmTab.html' },
            { title: "vApp", name: "vapp", state: "serviceTabs.vapp", active: false, disabled: true, templ: VAMI_ROOT + '/views/tabs/vApp.html' },
            { title: "System", name: "system", state: "serviceTabs.system", active: true, disabled: false, templ: VAMI_ROOT + '/views/tabs/systemTab.html'},
            { title: "Network", name: "network", state: "serviceTabs.network", active: false, disabled: false, templ: VAMI_ROOT + '/views/tabs/networkTab.html' }
        ];

        $scope.getActive = function () {
            return $scope.tabs.filter(function (tab) {
                return tab.active;
            })[0];
        };

        $scope.switchTab = function () {
            var state = $state.current;
            var active = $scope.active();
            if (active.state != state.name) {
                $log.debug("Transitioning to: " + state.name);
                $state.go(state.name, null, {notify: false, location: true});
            }
        };

        $scope.activeByState = function (tab) {
            var stateURL = _.last($state.current.url.split('/'));
            if (tab) {
                return (tab.name == stateURL);
            }
            return stateURL;
        };

        $scope.navType = 'pills';

    }]);
