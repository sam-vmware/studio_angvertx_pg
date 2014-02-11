'use strict';

vamiApp.lazy.factory('serviceTabsService', function serviceTabsService() {
        return {
            getAllTabs: function() {
                return [
                    { title:"System", content:"System Service" },
                    { title:"Network", content:"Network Service", disabled: false },
                    { title:"VM", content:"Virtual Machine", disabled: false },
                    { title:"vApp", content:"Virtual Appliance", disabled: false }
                ]
            },
            getTab: function(name) {
            }
        }
  });
