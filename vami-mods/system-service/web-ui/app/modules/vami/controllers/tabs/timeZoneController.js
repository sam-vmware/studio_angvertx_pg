'use strict';
/**
 * Created by samueldoyle
 */
systemApp.lazy.controller('timeZoneController', ['$q', '$scope', '$routeParams', '$log', '$timeout', '$modal',
    'COMMON_ROOT', 'systemTabsService',
    function ($q, $scope, $routeParams, $log, $timeout, $modal, COMMON_ROOT, systemTabsService) {

        var SET_TZ_OP = "Save Settings";

        $scope.$watch('selectedTimeZone', function (newSelect, oldSelect) {
            $log.debug("Selected TimeZone set to: " + newSelect.value);
            setTimeZoneMessage.data.newTimeZone = newSelect.value;
            setTimeZoneTestMessage.data.newTimeZone = newSelect.value;
        });

        // Do reboot and shutdown send test request, careful not to send the real one
        $scope.setTimeZone = function () {
            $log.debug("Setting TimeZone");

            var opConfirmedCB = function () {
                $log.debug("Set TimeZone operation confirmed sending request");
                serviceRequest({
                    serviceName: "system",
                    jsonMsg: setTimeZoneTestMessage
                }, function (reply) {
                    $log.debug("Set TimeZone request response received");
                    if (reply.data) {
                        $log.debug("reply.data -> " + reply.data);
                    }
                });
            };

            var opRejectedCB = function () {
                $log.debug("Set TimeZone operation rejected, request will not be sent");
            };

            $scope.open({confirmText: SET_TZ_OP}, opConfirmedCB, opRejectedCB);
        };

        $scope.getTimeZone = function () {
            $log.debug("Getting TimeZone");

            serviceRequest({
                serviceName: "system",
                jsonMsg: getTimeZoneMessage
            }, function (reply) {
                $log.debug("Get TimeZone request response received");
                if (reply.data) {
                    var targetSelect = _.arrayObjectIndexOf($scope.timeZones, reply.data.trim(), "value");
                    $log.debug("targetSelect: " + targetSelect);
                    if (targetSelect) {
                        $timeout(function () {
                            $scope.$apply(function () {
                                $scope.selectedTimeZone = $scope.timeZones[targetSelect];
                            });
                        }, 1000);
                    }
                }
            });
        };

        $scope.open = function (modalData, confirmCB, rejectedCB) {
            var modalInstance = $modal.open({
                templateUrl: COMMON_ROOT + '/views/dialog/confirmDialog.html',
                controller: 'confirmDialogController',
                resolve: {
                    modalData: function () {
                        return modalData;
//                        return $scope.modalData;
                    }
                }
            });

            modalInstance.result.then(function (selectedItem) {
                // promise resolved in the dialog i.e. comfirmed
                //$scope.selected = selectedItem;
                confirmCB();
            }, function () {
                // promise rejected in the dialog i.e. cancel
                rejectedCB();
            });
        };

        // Service request, send the options (message) and the callback on completion
        function serviceRequest(options, scopeApplyCallback) {

            // We get back a promise
            // Options must be of form e.g.
            // {
            //  serviceName: "system",
            //  jsonMsg: testGetSystemInformation
            // }
            systemTabsService.sendRequest(options).then(function (reply) {
                var timer = $timeout(function () {
                    $scope.$apply(function () {
                        if (scopeApplyCallback) {
                            scopeApplyCallback(reply);
                        }
                    });
                }, 1000);

                timer.then(
                    function () {
                        $log.debug("Timer resolved!", Date.now());
                    },
                    function () {
                        $log.warn("Timer rejected!", Date.now());
                    }
                );

                // Cleanup timer
                $scope.$on(
                    "$destroy",
                    function (event) {
                        if (timer) {
                            $timeout.cancel(timer);
                        }
                    }
                );

            });
        }

        $scope.timeZones = [
            { value: "Africa/Abidjan", id: "vami-debug-system-timezone-timezonelist-item0" },
            { value: "Africa/Accra", id: "vami-debug-system-timezone-timezonelist-item1" },
            { value: "Africa/Addis_Ababa", id: "vami-debug-system-timezone-timezonelist-item2" },
            { value: "Africa/Algiers", id: "vami-debug-system-timezone-timezonelist-item3" },
            { value: "Africa/Asmera", id: "vami-debug-system-timezone-timezonelist-item4" },
            { value: "Africa/Bamako", id: "vami-debug-system-timezone-timezonelist-item5" },
            { value: "Africa/Bangui", id: "vami-debug-system-timezone-timezonelist-item6" },
            { value: "Africa/Banjul", id: "vami-debug-system-timezone-timezonelist-item7" },
            { value: "Africa/Bissau", id: "vami-debug-system-timezone-timezonelist-item8" },
            { value: "Africa/Blantyre", id: "vami-debug-system-timezone-timezonelist-item9" },
            { value: "Africa/Brazzaville", id: "vami-debug-system-timezone-timezonelist-item10" },
            { value: "Africa/Bujumbura", id: "vami-debug-system-timezone-timezonelist-item11" },
            { value: "Africa/Cairo", id: "vami-debug-system-timezone-timezonelist-item12" },
            { value: "Africa/Casablanca", id: "vami-debug-system-timezone-timezonelist-item13" },
            { value: "Africa/Ceuta", id: "vami-debug-system-timezone-timezonelist-item14" },
            { value: "Africa/Conakry", id: "vami-debug-system-timezone-timezonelist-item15" },
            { value: "Africa/Dakar", id: "vami-debug-system-timezone-timezonelist-item16" },
            { value: "Africa/Dar_es_Salaam", id: "vami-debug-system-timezone-timezonelist-item17" },
            { value: "Africa/Djibouti", id: "vami-debug-system-timezone-timezonelist-item18" },
            { value: "Africa/Douala", id: "vami-debug-system-timezone-timezonelist-item19" },
            { value: "Africa/El_Aaiun", id: "vami-debug-system-timezone-timezonelist-item20" },
            { value: "Africa/Freetown", id: "vami-debug-system-timezone-timezonelist-item21" },
            { value: "Africa/Gaborone", id: "vami-debug-system-timezone-timezonelist-item22" },
            { value: "Africa/Harare", id: "vami-debug-system-timezone-timezonelist-item23" },
            { value: "Africa/Johannesburg", id: "vami-debug-system-timezone-timezonelist-item24" },
            { value: "Africa/Kampala", id: "vami-debug-system-timezone-timezonelist-item25" },
            { value: "Africa/Khartoum", id: "vami-debug-system-timezone-timezonelist-item26" },
            { value: "Africa/Kigali", id: "vami-debug-system-timezone-timezonelist-item27" },
            { value: "Africa/Kinshasa", id: "vami-debug-system-timezone-timezonelist-item28" },
            { value: "Africa/Lagos", id: "vami-debug-system-timezone-timezonelist-item29" },
            { value: "Africa/Libreville", id: "vami-debug-system-timezone-timezonelist-item30" },
            { value: "Africa/Lome", id: "vami-debug-system-timezone-timezonelist-item31" },
            { value: "Africa/Luanda", id: "vami-debug-system-timezone-timezonelist-item32" },
            { value: "Africa/Lubumbashi", id: "vami-debug-system-timezone-timezonelist-item33" },
            { value: "Africa/Lusaka", id: "vami-debug-system-timezone-timezonelist-item34" },
            { value: "Africa/Malabo", id: "vami-debug-system-timezone-timezonelist-item35" },
            { value: "Africa/Maputo", id: "vami-debug-system-timezone-timezonelist-item36" },
            { value: "Africa/Maseru", id: "vami-debug-system-timezone-timezonelist-item37" },
            { value: "Africa/Mbabane", id: "vami-debug-system-timezone-timezonelist-item38" },
            { value: "Africa/Mogadishu", id: "vami-debug-system-timezone-timezonelist-item39" },
            { value: "Africa/Monrovia", id: "vami-debug-system-timezone-timezonelist-item40" },
            { value: "Africa/Nairobi", id: "vami-debug-system-timezone-timezonelist-item41" },
            { value: "Africa/Ndjamena", id: "vami-debug-system-timezone-timezonelist-item42" },
            { value: "Africa/Niamey", id: "vami-debug-system-timezone-timezonelist-item43" },
            { value: "Africa/Nouakchott", id: "vami-debug-system-timezone-timezonelist-item44" },
            { value: "Africa/Ouagadougou", id: "vami-debug-system-timezone-timezonelist-item45" },
            { value: "Africa/Porto-Novo", id: "vami-debug-system-timezone-timezonelist-item46" },
            { value: "Africa/Sao_Tome", id: "vami-debug-system-timezone-timezonelist-item47" },
            { value: "Africa/Timbuktu", id: "vami-debug-system-timezone-timezonelist-item48" },
            { value: "Africa/Tripoli", id: "vami-debug-system-timezone-timezonelist-item49" },
            { value: "Africa/Tunis", id: "vami-debug-system-timezone-timezonelist-item50" },
            { value: "Africa/Windhoek", id: "vami-debug-system-timezone-timezonelist-item51" },
            { value: "America/Adak", id: "vami-debug-system-timezone-timezonelist-item52" },
            { value: "America/Anchorage", id: "vami-debug-system-timezone-timezonelist-item53" },
            { value: "America/Anguilla", id: "vami-debug-system-timezone-timezonelist-item54" },
            { value: "America/Antigua", id: "vami-debug-system-timezone-timezonelist-item55" },
            { value: "America/Araguaina", id: "vami-debug-system-timezone-timezonelist-item56" },
            { value: "America/Argentina/Buenos_Aires", id: "vami-debug-system-timezone-timezonelist-item57" },
            { value: "America/Argentina/Catamarca", id: "vami-debug-system-timezone-timezonelist-item58" },
            { value: "America/Argentina/ComodRivadavia", id: "vami-debug-system-timezone-timezonelist-item59" },
            { value: "America/Argentina/Cordoba", id: "vami-debug-system-timezone-timezonelist-item60" },
            { value: "America/Argentina/Jujuy", id: "vami-debug-system-timezone-timezonelist-item61" },
            { value: "America/Argentina/La_Rioja", id: "vami-debug-system-timezone-timezonelist-item62" },
            { value: "America/Argentina/Mendoza", id: "vami-debug-system-timezone-timezonelist-item63" },
            { value: "America/Argentina/Rio_Gallegos", id: "vami-debug-system-timezone-timezonelist-item64" },
            { value: "America/Argentina/San_Juan", id: "vami-debug-system-timezone-timezonelist-item65" },
            { value: "America/Argentina/Tucuman", id: "vami-debug-system-timezone-timezonelist-item66" },
            { value: "America/Argentina/Ushuaia", id: "vami-debug-system-timezone-timezonelist-item67" },
            { value: "America/Aruba", id: "vami-debug-system-timezone-timezonelist-item68" },
            { value: "America/Asuncion", id: "vami-debug-system-timezone-timezonelist-item69" },
            { value: "America/Atikokan", id: "vami-debug-system-timezone-timezonelist-item70" },
            { value: "America/Atka", id: "vami-debug-system-timezone-timezonelist-item71" },
            { value: "America/Bahia", id: "vami-debug-system-timezone-timezonelist-item72" },
            { value: "America/Barbados", id: "vami-debug-system-timezone-timezonelist-item73" },
            { value: "America/Belem", id: "vami-debug-system-timezone-timezonelist-item74" },
            { value: "America/Belize", id: "vami-debug-system-timezone-timezonelist-item75" },
            { value: "America/Blanc-Sablon", id: "vami-debug-system-timezone-timezonelist-item76" },
            { value: "America/Boa_Vista", id: "vami-debug-system-timezone-timezonelist-item77" },
            { value: "America/Bogota", id: "vami-debug-system-timezone-timezonelist-item78" },
            { value: "America/Boise", id: "vami-debug-system-timezone-timezonelist-item79" },
            { value: "America/Buenos_Aires", id: "vami-debug-system-timezone-timezonelist-item80" },
            { value: "America/Cambridge_Bay", id: "vami-debug-system-timezone-timezonelist-item81" },
            { value: "America/Campo_Grande", id: "vami-debug-system-timezone-timezonelist-item82" },
            { value: "America/Cancun", id: "vami-debug-system-timezone-timezonelist-item83" },
            { value: "America/Caracas", id: "vami-debug-system-timezone-timezonelist-item84" },
            { value: "America/Catamarca", id: "vami-debug-system-timezone-timezonelist-item85" },
            { value: "America/Cayenne", id: "vami-debug-system-timezone-timezonelist-item86" },
            { value: "America/Cayman", id: "vami-debug-system-timezone-timezonelist-item87" },
            { value: "America/Chicago", id: "vami-debug-system-timezone-timezonelist-item88" },
            { value: "America/Chihuahua", id: "vami-debug-system-timezone-timezonelist-item89" },
            { value: "America/Coral_Harbour", id: "vami-debug-system-timezone-timezonelist-item90" },
            { value: "America/Cordoba", id: "vami-debug-system-timezone-timezonelist-item91" },
            { value: "America/Costa_Rica", id: "vami-debug-system-timezone-timezonelist-item92" },
            { value: "America/Cuiaba", id: "vami-debug-system-timezone-timezonelist-item93" },
            { value: "America/Curacao", id: "vami-debug-system-timezone-timezonelist-item94" },
            { value: "America/Danmarkshavn", id: "vami-debug-system-timezone-timezonelist-item95" },
            { value: "America/Dawson", id: "vami-debug-system-timezone-timezonelist-item96" },
            { value: "America/Dawson_Creek", id: "vami-debug-system-timezone-timezonelist-item97" },
            { value: "America/Denver", id: "vami-debug-system-timezone-timezonelist-item98" },
            { value: "America/Detroit", id: "vami-debug-system-timezone-timezonelist-item99" },
            { value: "America/Dominica", id: "vami-debug-system-timezone-timezonelist-item100" },
            { value: "America/Edmonton", id: "vami-debug-system-timezone-timezonelist-item101" },
            { value: "America/Eirunepe", id: "vami-debug-system-timezone-timezonelist-item102" },
            { value: "America/El_Salvador", id: "vami-debug-system-timezone-timezonelist-item103" },
            { value: "America/Ensenada", id: "vami-debug-system-timezone-timezonelist-item104" },
            { value: "America/Fortaleza", id: "vami-debug-system-timezone-timezonelist-item105" },
            { value: "America/Fort_Wayne", id: "vami-debug-system-timezone-timezonelist-item106" },
            { value: "America/Glace_Bay", id: "vami-debug-system-timezone-timezonelist-item107" },
            { value: "America/Godthab", id: "vami-debug-system-timezone-timezonelist-item108" },
            { value: "America/Goose_Bay", id: "vami-debug-system-timezone-timezonelist-item109" },
            { value: "America/Grand_Turk", id: "vami-debug-system-timezone-timezonelist-item110" },
            { value: "America/Grenada", id: "vami-debug-system-timezone-timezonelist-item111" },
            { value: "America/Guadeloupe", id: "vami-debug-system-timezone-timezonelist-item112" },
            { value: "America/Guatemala", id: "vami-debug-system-timezone-timezonelist-item113" },
            { value: "America/Guayaquil", id: "vami-debug-system-timezone-timezonelist-item114" },
            { value: "America/Guyana", id: "vami-debug-system-timezone-timezonelist-item115" },
            { value: "America/Halifax", id: "vami-debug-system-timezone-timezonelist-item116" },
            { value: "America/Havana", id: "vami-debug-system-timezone-timezonelist-item117" },
            { value: "America/Hermosillo", id: "vami-debug-system-timezone-timezonelist-item118" },
            { value: "America/Indiana/Indianapolis", id: "vami-debug-system-timezone-timezonelist-item119" },
            { value: "America/Indiana/Knox", id: "vami-debug-system-timezone-timezonelist-item120" },
            { value: "America/Indiana/Marengo", id: "vami-debug-system-timezone-timezonelist-item121" },
            { value: "America/Indiana/Petersburg", id: "vami-debug-system-timezone-timezonelist-item122" },
            { value: "America/Indianapolis", id: "vami-debug-system-timezone-timezonelist-item123" },
            { value: "America/Indiana/Vevay", id: "vami-debug-system-timezone-timezonelist-item124" },
            { value: "America/Indiana/Vincennes", id: "vami-debug-system-timezone-timezonelist-item125" },
            { value: "America/Inuvik", id: "vami-debug-system-timezone-timezonelist-item126" },
            { value: "America/Iqaluit", id: "vami-debug-system-timezone-timezonelist-item127" },
            { value: "America/Jamaica", id: "vami-debug-system-timezone-timezonelist-item128" },
            { value: "America/Jujuy", id: "vami-debug-system-timezone-timezonelist-item129" },
            { value: "America/Juneau", id: "vami-debug-system-timezone-timezonelist-item130" },
            { value: "America/Kentucky/Louisville", id: "vami-debug-system-timezone-timezonelist-item131" },
            { value: "America/Kentucky/Monticello", id: "vami-debug-system-timezone-timezonelist-item132" },
            { value: "America/Knox_IN", id: "vami-debug-system-timezone-timezonelist-item133" },
            { value: "America/La_Paz", id: "vami-debug-system-timezone-timezonelist-item134" },
            { value: "America/Lima", id: "vami-debug-system-timezone-timezonelist-item135" },
            { value: "America/Los_Angeles", id: "vami-debug-system-timezone-timezonelist-item136" },
            { value: "America/Louisville", id: "vami-debug-system-timezone-timezonelist-item137" },
            { value: "America/Maceio", id: "vami-debug-system-timezone-timezonelist-item138" },
            { value: "America/Managua", id: "vami-debug-system-timezone-timezonelist-item139" },
            { value: "America/Manaus", id: "vami-debug-system-timezone-timezonelist-item140" },
            { value: "America/Martinique", id: "vami-debug-system-timezone-timezonelist-item141" },
            { value: "America/Mazatlan", id: "vami-debug-system-timezone-timezonelist-item142" },
            { value: "America/Mendoza", id: "vami-debug-system-timezone-timezonelist-item143" },
            { value: "America/Menominee", id: "vami-debug-system-timezone-timezonelist-item144" },
            { value: "America/Merida", id: "vami-debug-system-timezone-timezonelist-item145" },
            { value: "America/Mexico_City", id: "vami-debug-system-timezone-timezonelist-item146" },
            { value: "America/Miquelon", id: "vami-debug-system-timezone-timezonelist-item147" },
            { value: "America/Moncton", id: "vami-debug-system-timezone-timezonelist-item148" },
            { value: "America/Monterrey", id: "vami-debug-system-timezone-timezonelist-item149" },
            { value: "America/Montevideo", id: "vami-debug-system-timezone-timezonelist-item150" },
            { value: "America/Montreal", id: "vami-debug-system-timezone-timezonelist-item151" },
            { value: "America/Montserrat", id: "vami-debug-system-timezone-timezonelist-item152" },
            { value: "America/Nassau", id: "vami-debug-system-timezone-timezonelist-item153" },
            { value: "America/New_York", id: "vami-debug-system-timezone-timezonelist-item154" },
            { value: "America/Nipigon", id: "vami-debug-system-timezone-timezonelist-item155" },
            { value: "America/Nome", id: "vami-debug-system-timezone-timezonelist-item156" },
            { value: "America/Noronha", id: "vami-debug-system-timezone-timezonelist-item157" },
            { value: "America/North_Dakota/Center", id: "vami-debug-system-timezone-timezonelist-item158" },
            { value: "America/North_Dakota/New_Salem", id: "vami-debug-system-timezone-timezonelist-item159" },
            { value: "America/Panama", id: "vami-debug-system-timezone-timezonelist-item160" },
            { value: "America/Pangnirtung", id: "vami-debug-system-timezone-timezonelist-item161" },
            { value: "America/Paramaribo", id: "vami-debug-system-timezone-timezonelist-item162" },
            { value: "America/Phoenix", id: "vami-debug-system-timezone-timezonelist-item163" },
            { value: "America/Port-au-Prince", id: "vami-debug-system-timezone-timezonelist-item164" },
            { value: "America/Porto_Acre", id: "vami-debug-system-timezone-timezonelist-item165" },
            { value: "America/Port_of_Spain", id: "vami-debug-system-timezone-timezonelist-item166" },
            { value: "America/Porto_Velho", id: "vami-debug-system-timezone-timezonelist-item167" },
            { value: "America/Puerto_Rico", id: "vami-debug-system-timezone-timezonelist-item168" },
            { value: "America/Rainy_River", id: "vami-debug-system-timezone-timezonelist-item169" },
            { value: "America/Rankin_Inlet", id: "vami-debug-system-timezone-timezonelist-item170" },
            { value: "America/Recife", id: "vami-debug-system-timezone-timezonelist-item171" },
            { value: "America/Regina", id: "vami-debug-system-timezone-timezonelist-item172" },
            { value: "America/Rio_Branco", id: "vami-debug-system-timezone-timezonelist-item173" },
            { value: "America/Rosario", id: "vami-debug-system-timezone-timezonelist-item174" },
            { value: "America/Santiago", id: "vami-debug-system-timezone-timezonelist-item175" },
            { value: "America/Santo_Domingo", id: "vami-debug-system-timezone-timezonelist-item176" },
            { value: "America/Sao_Paulo", id: "vami-debug-system-timezone-timezonelist-item177" },
            { value: "America/Scoresbysund", id: "vami-debug-system-timezone-timezonelist-item178" },
            { value: "America/Shiprock", id: "vami-debug-system-timezone-timezonelist-item179" },
            { value: "America/St_Johns", id: "vami-debug-system-timezone-timezonelist-item180" },
            { value: "America/St_Kitts", id: "vami-debug-system-timezone-timezonelist-item181" },
            { value: "America/St_Lucia", id: "vami-debug-system-timezone-timezonelist-item182" },
            { value: "America/St_Thomas", id: "vami-debug-system-timezone-timezonelist-item183" },
            { value: "America/St_Vincent", id: "vami-debug-system-timezone-timezonelist-item184" },
            { value: "America/Tegucigalpa", id: "vami-debug-system-timezone-timezonelist-item185" },
            { value: "America/Thule", id: "vami-debug-system-timezone-timezonelist-item186" },
            { value: "America/Thunder_Bay", id: "vami-debug-system-timezone-timezonelist-item187" },
            { value: "America/Tijuana", id: "vami-debug-system-timezone-timezonelist-item188" },
            { value: "America/Toronto", id: "vami-debug-system-timezone-timezonelist-item189" },
            { value: "America/Tortola", id: "vami-debug-system-timezone-timezonelist-item190" },
            { value: "America/Vancouver", id: "vami-debug-system-timezone-timezonelist-item191" },
            { value: "America/Virgin", id: "vami-debug-system-timezone-timezonelist-item192" },
            { value: "America/Whitehorse", id: "vami-debug-system-timezone-timezonelist-item193" },
            { value: "America/Winnipeg", id: "vami-debug-system-timezone-timezonelist-item194" },
            { value: "America/Yakutat", id: "vami-debug-system-timezone-timezonelist-item195" },
            { value: "America/Yellowknife", id: "vami-debug-system-timezone-timezonelist-item196" },
            { value: "Asia/Aden", id: "vami-debug-system-timezone-timezonelist-item197" },
            { value: "Asia/Almaty", id: "vami-debug-system-timezone-timezonelist-item198" },
            { value: "Asia/Amman", id: "vami-debug-system-timezone-timezonelist-item199" },
            { value: "Asia/Anadyr", id: "vami-debug-system-timezone-timezonelist-item200" },
            { value: "Asia/Aqtau", id: "vami-debug-system-timezone-timezonelist-item201" },
            { value: "Asia/Aqtobe", id: "vami-debug-system-timezone-timezonelist-item202" },
            { value: "Asia/Ashgabat", id: "vami-debug-system-timezone-timezonelist-item203" },
            { value: "Asia/Ashkhabad", id: "vami-debug-system-timezone-timezonelist-item204" },
            { value: "Asia/Baghdad", id: "vami-debug-system-timezone-timezonelist-item205" },
            { value: "Asia/Bahrain", id: "vami-debug-system-timezone-timezonelist-item206" },
            { value: "Asia/Baku", id: "vami-debug-system-timezone-timezonelist-item207" },
            { value: "Asia/Bangkok", id: "vami-debug-system-timezone-timezonelist-item208" },
            { value: "Asia/Beirut", id: "vami-debug-system-timezone-timezonelist-item209" },
            { value: "Asia/Bishkek", id: "vami-debug-system-timezone-timezonelist-item210" },
            { value: "Asia/Brunei", id: "vami-debug-system-timezone-timezonelist-item211" },
            { value: "Asia/Calcutta", id: "vami-debug-system-timezone-timezonelist-item212" },
            { value: "Asia/Choibalsan", id: "vami-debug-system-timezone-timezonelist-item213" },
            { value: "Asia/Chongqing", id: "vami-debug-system-timezone-timezonelist-item214" },
            { value: "Asia/Chungking", id: "vami-debug-system-timezone-timezonelist-item215" },
            { value: "Asia/Colombo", id: "vami-debug-system-timezone-timezonelist-item216" },
            { value: "Asia/Dacca", id: "vami-debug-system-timezone-timezonelist-item217" },
            { value: "Asia/Damascus", id: "vami-debug-system-timezone-timezonelist-item218" },
            { value: "Asia/Dhaka", id: "vami-debug-system-timezone-timezonelist-item219" },
            { value: "Asia/Dili", id: "vami-debug-system-timezone-timezonelist-item220" },
            { value: "Asia/Dubai", id: "vami-debug-system-timezone-timezonelist-item221" },
            { value: "Asia/Dushanbe", id: "vami-debug-system-timezone-timezonelist-item222" },
            { value: "Asia/Gaza", id: "vami-debug-system-timezone-timezonelist-item223" },
            { value: "Asia/Harbin", id: "vami-debug-system-timezone-timezonelist-item224" },
            { value: "Asia/Hong_Kong", id: "vami-debug-system-timezone-timezonelist-item225" },
            { value: "Asia/Hovd", id: "vami-debug-system-timezone-timezonelist-item226" },
            { value: "Asia/Irkutsk", id: "vami-debug-system-timezone-timezonelist-item227" },
            { value: "Asia/Istanbul", id: "vami-debug-system-timezone-timezonelist-item228" },
            { value: "Asia/Jakarta", id: "vami-debug-system-timezone-timezonelist-item229" },
            { value: "Asia/Jayapura", id: "vami-debug-system-timezone-timezonelist-item230" },
            { value: "Asia/Jerusalem", id: "vami-debug-system-timezone-timezonelist-item231" },
            { value: "Asia/Kabul", id: "vami-debug-system-timezone-timezonelist-item232" },
            { value: "Asia/Kamchatka", id: "vami-debug-system-timezone-timezonelist-item233" },
            { value: "Asia/Karachi", id: "vami-debug-system-timezone-timezonelist-item234" },
            { value: "Asia/Kashgar", id: "vami-debug-system-timezone-timezonelist-item235" },
            { value: "Asia/Katmandu", id: "vami-debug-system-timezone-timezonelist-item236" },
            { value: "Asia/Krasnoyarsk", id: "vami-debug-system-timezone-timezonelist-item237" },
            { value: "Asia/Kuala_Lumpur", id: "vami-debug-system-timezone-timezonelist-item238" },
            { value: "Asia/Kuching", id: "vami-debug-system-timezone-timezonelist-item239" },
            { value: "Asia/Kuwait", id: "vami-debug-system-timezone-timezonelist-item240" },
            { value: "Asia/Macao", id: "vami-debug-system-timezone-timezonelist-item241" },
            { value: "Asia/Macau", id: "vami-debug-system-timezone-timezonelist-item242" },
            { value: "Asia/Magadan", id: "vami-debug-system-timezone-timezonelist-item243" },
            { value: "Asia/Makassar", id: "vami-debug-system-timezone-timezonelist-item244" },
            { value: "Asia/Manila", id: "vami-debug-system-timezone-timezonelist-item245" },
            { value: "Asia/Muscat", id: "vami-debug-system-timezone-timezonelist-item246" },
            { value: "Asia/Nicosia", id: "vami-debug-system-timezone-timezonelist-item247" },
            { value: "Asia/Novosibirsk", id: "vami-debug-system-timezone-timezonelist-item248" },
            { value: "Asia/Omsk", id: "vami-debug-system-timezone-timezonelist-item249" },
            { value: "Asia/Oral", id: "vami-debug-system-timezone-timezonelist-item250" },
            { value: "Asia/Phnom_Penh", id: "vami-debug-system-timezone-timezonelist-item251" },
            { value: "Asia/Pontianak", id: "vami-debug-system-timezone-timezonelist-item252" },
            { value: "Asia/Pyongyang", id: "vami-debug-system-timezone-timezonelist-item253" },
            { value: "Asia/Qatar", id: "vami-debug-system-timezone-timezonelist-item254" },
            { value: "Asia/Qyzylorda", id: "vami-debug-system-timezone-timezonelist-item255" },
            { value: "Asia/Rangoon", id: "vami-debug-system-timezone-timezonelist-item256" },
            { value: "Asia/Riyadh", id: "vami-debug-system-timezone-timezonelist-item257" },
            { value: "Asia/Riyadh87", id: "vami-debug-system-timezone-timezonelist-item258" },
            { value: "Asia/Riyadh88", id: "vami-debug-system-timezone-timezonelist-item259" },
            { value: "Asia/Riyadh89", id: "vami-debug-system-timezone-timezonelist-item260" },
            { value: "Asia/Saigon", id: "vami-debug-system-timezone-timezonelist-item261" },
            { value: "Asia/Sakhalin", id: "vami-debug-system-timezone-timezonelist-item262" },
            { value: "Asia/Samarkand", id: "vami-debug-system-timezone-timezonelist-item263" },
            { value: "Asia/Seoul", id: "vami-debug-system-timezone-timezonelist-item264" },
            { value: "Asia/Shanghai", id: "vami-debug-system-timezone-timezonelist-item265" },
            { value: "Asia/Singapore", id: "vami-debug-system-timezone-timezonelist-item266" },
            { value: "Asia/Taipei", id: "vami-debug-system-timezone-timezonelist-item267" },
            { value: "Asia/Tashkent", id: "vami-debug-system-timezone-timezonelist-item268" },
            { value: "Asia/Tbilisi", id: "vami-debug-system-timezone-timezonelist-item269" },
            { value: "Asia/Tehran", id: "vami-debug-system-timezone-timezonelist-item270" },
            { value: "Asia/Tel_Aviv", id: "vami-debug-system-timezone-timezonelist-item271" },
            { value: "Asia/Thimbu", id: "vami-debug-system-timezone-timezonelist-item272" },
            { value: "Asia/Thimphu", id: "vami-debug-system-timezone-timezonelist-item273" },
            { value: "Asia/Tokyo", id: "vami-debug-system-timezone-timezonelist-item274" },
            { value: "Asia/Ujung_Pandang", id: "vami-debug-system-timezone-timezonelist-item275" },
            { value: "Asia/Ulaanbaatar", id: "vami-debug-system-timezone-timezonelist-item276" },
            { value: "Asia/Ulan_Bator", id: "vami-debug-system-timezone-timezonelist-item277" },
            { value: "Asia/Urumqi", id: "vami-debug-system-timezone-timezonelist-item278" },
            { value: "Asia/Vientiane", id: "vami-debug-system-timezone-timezonelist-item279" },
            { value: "Asia/Vladivostok", id: "vami-debug-system-timezone-timezonelist-item280" },
            { value: "Asia/Yakutsk", id: "vami-debug-system-timezone-timezonelist-item281" },
            { value: "Asia/Yekaterinburg", id: "vami-debug-system-timezone-timezonelist-item282" },
            { value: "Asia/Yerevan", id: "vami-debug-system-timezone-timezonelist-item283" },
            { value: "Australia/ACT", id: "vami-debug-system-timezone-timezonelist-item284" },
            { value: "Australia/Adelaide", id: "vami-debug-system-timezone-timezonelist-item285" },
            { value: "Australia/Brisbane", id: "vami-debug-system-timezone-timezonelist-item286" },
            { value: "Australia/Broken_Hill", id: "vami-debug-system-timezone-timezonelist-item287" },
            { value: "Australia/Canberra", id: "vami-debug-system-timezone-timezonelist-item288" },
            { value: "Australia/Currie", id: "vami-debug-system-timezone-timezonelist-item289" },
            { value: "Australia/Darwin", id: "vami-debug-system-timezone-timezonelist-item290" },
            { value: "Australia/Hobart", id: "vami-debug-system-timezone-timezonelist-item291" },
            { value: "Australia/LHI", id: "vami-debug-system-timezone-timezonelist-item292" },
            { value: "Australia/Lindeman", id: "vami-debug-system-timezone-timezonelist-item293" },
            { value: "Australia/Lord_Howe", id: "vami-debug-system-timezone-timezonelist-item294" },
            { value: "Australia/Melbourne", id: "vami-debug-system-timezone-timezonelist-item295" },
            { value: "Australia/North", id: "vami-debug-system-timezone-timezonelist-item296" },
            { value: "Australia/NSW", id: "vami-debug-system-timezone-timezonelist-item297" },
            { value: "Australia/Perth", id: "vami-debug-system-timezone-timezonelist-item298" },
            { value: "Australia/Queensland", id: "vami-debug-system-timezone-timezonelist-item299" },
            { value: "Australia/South", id: "vami-debug-system-timezone-timezonelist-item300" },
            { value: "Australia/Sydney", id: "vami-debug-system-timezone-timezonelist-item301" },
            { value: "Australia/Tasmania", id: "vami-debug-system-timezone-timezonelist-item302" },
            { value: "Australia/Victoria", id: "vami-debug-system-timezone-timezonelist-item303" },
            { value: "Australia/West", id: "vami-debug-system-timezone-timezonelist-item304" },
            { value: "Australia/Yancowinna", id: "vami-debug-system-timezone-timezonelist-item305" },
            { value: "Brazil/Acre", id: "vami-debug-system-timezone-timezonelist-item306" },
            { value: "Brazil/DeNoronha", id: "vami-debug-system-timezone-timezonelist-item307" },
            { value: "Brazil/East", id: "vami-debug-system-timezone-timezonelist-item308" },
            { value: "Brazil/West", id: "vami-debug-system-timezone-timezonelist-item309" },
            { value: "Canada/Atlantic", id: "vami-debug-system-timezone-timezonelist-item310" },
            { value: "Canada/Central", id: "vami-debug-system-timezone-timezonelist-item311" },
            { value: "Canada/Eastern", id: "vami-debug-system-timezone-timezonelist-item312" },
            { value: "Canada/East-Saskatchewan", id: "vami-debug-system-timezone-timezonelist-item313" },
            { value: "Canada/Mountain", id: "vami-debug-system-timezone-timezonelist-item314" },
            { value: "Canada/Newfoundland", id: "vami-debug-system-timezone-timezonelist-item315" },
            { value: "Canada/Pacific", id: "vami-debug-system-timezone-timezonelist-item316" },
            { value: "Canada/Saskatchewan", id: "vami-debug-system-timezone-timezonelist-item317" },
            { value: "Canada/Yukon", id: "vami-debug-system-timezone-timezonelist-item318" },
            { value: "Chile/Continental", id: "vami-debug-system-timezone-timezonelist-item319" },
            { value: "Chile/EasterIsland", id: "vami-debug-system-timezone-timezonelist-item320" },
            { value: "Etc/GMT", id: "vami-debug-system-timezone-timezonelist-item321" },
            { value: "Etc/GMT0", id: "vami-debug-system-timezone-timezonelist-item322" },
            { value: "Etc/GMT-0", id: "vami-debug-system-timezone-timezonelist-item323" },
            { value: "Etc/GMT+0", id: "vami-debug-system-timezone-timezonelist-item324" },
            { value: "Etc/GMT-1", id: "vami-debug-system-timezone-timezonelist-item325" },
            { value: "Etc/GMT+1", id: "vami-debug-system-timezone-timezonelist-item326" },
            { value: "Etc/GMT-10", id: "vami-debug-system-timezone-timezonelist-item327" },
            { value: "Etc/GMT+10", id: "vami-debug-system-timezone-timezonelist-item328" },
            { value: "Etc/GMT-11", id: "vami-debug-system-timezone-timezonelist-item329" },
            { value: "Etc/GMT+11", id: "vami-debug-system-timezone-timezonelist-item330" },
            { value: "Etc/GMT-12", id: "vami-debug-system-timezone-timezonelist-item331" },
            { value: "Etc/GMT+12", id: "vami-debug-system-timezone-timezonelist-item332" },
            { value: "Etc/GMT-13", id: "vami-debug-system-timezone-timezonelist-item333" },
            { value: "Etc/GMT-14", id: "vami-debug-system-timezone-timezonelist-item334" },
            { value: "Etc/GMT-2", id: "vami-debug-system-timezone-timezonelist-item335" },
            { value: "Etc/GMT+2", id: "vami-debug-system-timezone-timezonelist-item336" },
            { value: "Etc/GMT-3", id: "vami-debug-system-timezone-timezonelist-item337" },
            { value: "Etc/GMT+3", id: "vami-debug-system-timezone-timezonelist-item338" },
            { value: "Etc/GMT-4", id: "vami-debug-system-timezone-timezonelist-item339" },
            { value: "Etc/GMT+4", id: "vami-debug-system-timezone-timezonelist-item340" },
            { value: "Etc/GMT-5", id: "vami-debug-system-timezone-timezonelist-item341" },
            { value: "Etc/GMT+5", id: "vami-debug-system-timezone-timezonelist-item342" },
            { value: "Etc/GMT-6", id: "vami-debug-system-timezone-timezonelist-item343" },
            { value: "Etc/GMT+6", id: "vami-debug-system-timezone-timezonelist-item344" },
            { value: "Etc/GMT-7", id: "vami-debug-system-timezone-timezonelist-item345" },
            { value: "Etc/GMT+7", id: "vami-debug-system-timezone-timezonelist-item346" },
            { value: "Etc/GMT-8", id: "vami-debug-system-timezone-timezonelist-item347" },
            { value: "Etc/GMT+8", id: "vami-debug-system-timezone-timezonelist-item348" },
            { value: "Etc/GMT-9", id: "vami-debug-system-timezone-timezonelist-item349" },
            { value: "Etc/GMT+9", id: "vami-debug-system-timezone-timezonelist-item350" },
            { value: "Etc/Greenwich", id: "vami-debug-system-timezone-timezonelist-item351" },
            { value: "Etc/UCT", id: "vami-debug-system-timezone-timezonelist-item352" },
            { value: "Etc/Universal", id: "vami-debug-system-timezone-timezonelist-item353" },
            { value: "Etc/UTC", id: "vami-debug-system-timezone-timezonelist-item354" },
            { value: "Etc/Zulu", id: "vami-debug-system-timezone-timezonelist-item355" },
            { value: "Europe/Amsterdam", id: "vami-debug-system-timezone-timezonelist-item356" },
            { value: "Europe/Andorra", id: "vami-debug-system-timezone-timezonelist-item357" },
            { value: "Europe/Athens", id: "vami-debug-system-timezone-timezonelist-item358" },
            { value: "Europe/Belfast", id: "vami-debug-system-timezone-timezonelist-item359" },
            { value: "Europe/Belgrade", id: "vami-debug-system-timezone-timezonelist-item360" },
            { value: "Europe/Berlin", id: "vami-debug-system-timezone-timezonelist-item361" },
            { value: "Europe/Bratislava", id: "vami-debug-system-timezone-timezonelist-item362" },
            { value: "Europe/Brussels", id: "vami-debug-system-timezone-timezonelist-item363" },
            { value: "Europe/Bucharest", id: "vami-debug-system-timezone-timezonelist-item364" },
            { value: "Europe/Budapest", id: "vami-debug-system-timezone-timezonelist-item365" },
            { value: "Europe/Chisinau", id: "vami-debug-system-timezone-timezonelist-item366" },
            { value: "Europe/Copenhagen", id: "vami-debug-system-timezone-timezonelist-item367" },
            { value: "Europe/Dublin", id: "vami-debug-system-timezone-timezonelist-item368" },
            { value: "Europe/Gibraltar", id: "vami-debug-system-timezone-timezonelist-item369" },
            { value: "Europe/Guernsey", id: "vami-debug-system-timezone-timezonelist-item370" },
            { value: "Europe/Helsinki", id: "vami-debug-system-timezone-timezonelist-item371" },
            { value: "Europe/Isle_of_Man", id: "vami-debug-system-timezone-timezonelist-item372" },
            { value: "Europe/Istanbul", id: "vami-debug-system-timezone-timezonelist-item373" },
            { value: "Europe/Jersey", id: "vami-debug-system-timezone-timezonelist-item374" },
            { value: "Europe/Kaliningrad", id: "vami-debug-system-timezone-timezonelist-item375" },
            { value: "Europe/Kiev", id: "vami-debug-system-timezone-timezonelist-item376" },
            { value: "Europe/Lisbon", id: "vami-debug-system-timezone-timezonelist-item377" },
            { value: "Europe/Ljubljana", id: "vami-debug-system-timezone-timezonelist-item378" },
            { value: "Europe/London", id: "vami-debug-system-timezone-timezonelist-item379" },
            { value: "Europe/Luxembourg", id: "vami-debug-system-timezone-timezonelist-item380" },
            { value: "Europe/Madrid", id: "vami-debug-system-timezone-timezonelist-item381" },
            { value: "Europe/Malta", id: "vami-debug-system-timezone-timezonelist-item382" },
            { value: "Europe/Mariehamn", id: "vami-debug-system-timezone-timezonelist-item383" },
            { value: "Europe/Minsk", id: "vami-debug-system-timezone-timezonelist-item384" },
            { value: "Europe/Monaco", id: "vami-debug-system-timezone-timezonelist-item385" },
            { value: "Europe/Moscow", id: "vami-debug-system-timezone-timezonelist-item386" },
            { value: "Europe/Nicosia", id: "vami-debug-system-timezone-timezonelist-item387" },
            { value: "Europe/Oslo", id: "vami-debug-system-timezone-timezonelist-item388" },
            { value: "Europe/Paris", id: "vami-debug-system-timezone-timezonelist-item389" },
            { value: "Europe/Podgorica", id: "vami-debug-system-timezone-timezonelist-item390" },
            { value: "Europe/Prague", id: "vami-debug-system-timezone-timezonelist-item391" },
            { value: "Europe/Riga", id: "vami-debug-system-timezone-timezonelist-item392" },
            { value: "Europe/Rome", id: "vami-debug-system-timezone-timezonelist-item393" },
            { value: "Europe/Samara", id: "vami-debug-system-timezone-timezonelist-item394" },
            { value: "Europe/San_Marino", id: "vami-debug-system-timezone-timezonelist-item395" },
            { value: "Europe/Sarajevo", id: "vami-debug-system-timezone-timezonelist-item396" },
            { value: "Europe/Simferopol", id: "vami-debug-system-timezone-timezonelist-item397" },
            { value: "Europe/Skopje", id: "vami-debug-system-timezone-timezonelist-item398" },
            { value: "Europe/Sofia", id: "vami-debug-system-timezone-timezonelist-item399" },
            { value: "Europe/Stockholm", id: "vami-debug-system-timezone-timezonelist-item400" },
            { value: "Europe/Tallinn", id: "vami-debug-system-timezone-timezonelist-item401" },
            { value: "Europe/Tirane", id: "vami-debug-system-timezone-timezonelist-item402" },
            { value: "Europe/Tiraspol", id: "vami-debug-system-timezone-timezonelist-item403" },
            { value: "Europe/Uzhgorod", id: "vami-debug-system-timezone-timezonelist-item404" },
            { value: "Europe/Vaduz", id: "vami-debug-system-timezone-timezonelist-item405" },
            { value: "Europe/Vatican", id: "vami-debug-system-timezone-timezonelist-item406" },
            { value: "Europe/Vienna", id: "vami-debug-system-timezone-timezonelist-item407" },
            { value: "Europe/Vilnius", id: "vami-debug-system-timezone-timezonelist-item408" },
            { value: "Europe/Volgograd", id: "vami-debug-system-timezone-timezonelist-item409" },
            { value: "Europe/Warsaw", id: "vami-debug-system-timezone-timezonelist-item410" },
            { value: "Europe/Zagreb", id: "vami-debug-system-timezone-timezonelist-item411" },
            { value: "Europe/Zaporozhye", id: "vami-debug-system-timezone-timezonelist-item412" },
            { value: "Europe/Zurich", id: "vami-debug-system-timezone-timezonelist-item413" },
            { value: "GB", id: "vami-debug-system-timezone-timezonelist-item414" },
            { value: "GB-Eire", id: "vami-debug-system-timezone-timezonelist-item415" },
            { value: "GMT", id: "vami-debug-system-timezone-timezonelist-item416" },
            { value: "GMT0", id: "vami-debug-system-timezone-timezonelist-item417" },
            { value: "GMT-0", id: "vami-debug-system-timezone-timezonelist-item418" },
            { value: "GMT+0", id: "vami-debug-system-timezone-timezonelist-item419" },
            { value: "Greenwich", id: "vami-debug-system-timezone-timezonelist-item420" },
            { value: "Hongkong", id: "vami-debug-system-timezone-timezonelist-item421" },
            { value: "Iceland", id: "vami-debug-system-timezone-timezonelist-item422" },
            { value: "Iran", id: "vami-debug-system-timezone-timezonelist-item423" },
            { value: "Israel", id: "vami-debug-system-timezone-timezonelist-item424" },
            { value: "Jamaica", id: "vami-debug-system-timezone-timezonelist-item425" },
            { value: "Japan", id: "vami-debug-system-timezone-timezonelist-item426" },
            { value: "Kwajalein", id: "vami-debug-system-timezone-timezonelist-item427" },
            { value: "Libya", id: "vami-debug-system-timezone-timezonelist-item428" },
            { value: "Mexico/BajaNorte", id: "vami-debug-system-timezone-timezonelist-item429" },
            { value: "Mexico/BajaSur", id: "vami-debug-system-timezone-timezonelist-item430" },
            { value: "Mexico/General", id: "vami-debug-system-timezone-timezonelist-item431" },
            { value: "Pacific/Apia", id: "vami-debug-system-timezone-timezonelist-item432" },
            { value: "Pacific/Auckland", id: "vami-debug-system-timezone-timezonelist-item433" },
            { value: "Pacific/Chatham", id: "vami-debug-system-timezone-timezonelist-item434" },
            { value: "Pacific/Easter", id: "vami-debug-system-timezone-timezonelist-item435" },
            { value: "Pacific/Efate", id: "vami-debug-system-timezone-timezonelist-item436" },
            { value: "Pacific/Enderbury", id: "vami-debug-system-timezone-timezonelist-item437" },
            { value: "Pacific/Fakaofo", id: "vami-debug-system-timezone-timezonelist-item438" },
            { value: "Pacific/Fiji", id: "vami-debug-system-timezone-timezonelist-item439" },
            { value: "Pacific/Funafuti", id: "vami-debug-system-timezone-timezonelist-item440" },
            { value: "Pacific/Galapagos", id: "vami-debug-system-timezone-timezonelist-item441" },
            { value: "Pacific/Gambier", id: "vami-debug-system-timezone-timezonelist-item442" },
            { value: "Pacific/Guadalcanal", id: "vami-debug-system-timezone-timezonelist-item443" },
            { value: "Pacific/Guam", id: "vami-debug-system-timezone-timezonelist-item444" },
            { value: "Pacific/Honolulu", id: "vami-debug-system-timezone-timezonelist-item445" },
            { value: "Pacific/Johnston", id: "vami-debug-system-timezone-timezonelist-item446" },
            { value: "Pacific/Kiritimati", id: "vami-debug-system-timezone-timezonelist-item447" },
            { value: "Pacific/Kosrae", id: "vami-debug-system-timezone-timezonelist-item448" },
            { value: "Pacific/Kwajalein", id: "vami-debug-system-timezone-timezonelist-item449" },
            { value: "Pacific/Majuro", id: "vami-debug-system-timezone-timezonelist-item450" },
            { value: "Pacific/Marquesas", id: "vami-debug-system-timezone-timezonelist-item451" },
            { value: "Pacific/Midway", id: "vami-debug-system-timezone-timezonelist-item452" },
            { value: "Pacific/Nauru", id: "vami-debug-system-timezone-timezonelist-item453" },
            { value: "Pacific/Niue", id: "vami-debug-system-timezone-timezonelist-item454" },
            { value: "Pacific/Norfolk", id: "vami-debug-system-timezone-timezonelist-item455" },
            { value: "Pacific/Noumea", id: "vami-debug-system-timezone-timezonelist-item456" },
            { value: "Pacific/Pago_Pago", id: "vami-debug-system-timezone-timezonelist-item457" },
            { value: "Pacific/Palau", id: "vami-debug-system-timezone-timezonelist-item458" },
            { value: "Pacific/Pitcairn", id: "vami-debug-system-timezone-timezonelist-item459" },
            { value: "Pacific/Ponape", id: "vami-debug-system-timezone-timezonelist-item460" },
            { value: "Pacific/Port_Moresby", id: "vami-debug-system-timezone-timezonelist-item461" },
            { value: "Pacific/Rarotonga", id: "vami-debug-system-timezone-timezonelist-item462" },
            { value: "Pacific/Saipan", id: "vami-debug-system-timezone-timezonelist-item463" },
            { value: "Pacific/Samoa", id: "vami-debug-system-timezone-timezonelist-item464" },
            { value: "Pacific/Tahiti", id: "vami-debug-system-timezone-timezonelist-item465" },
            { value: "Pacific/Tarawa", id: "vami-debug-system-timezone-timezonelist-item466" },
            { value: "Pacific/Tongatapu", id: "vami-debug-system-timezone-timezonelist-item467" },
            { value: "Pacific/Truk", id: "vami-debug-system-timezone-timezonelist-item468" },
            { value: "Pacific/Wake", id: "vami-debug-system-timezone-timezonelist-item469" },
            { value: "Pacific/Wallis", id: "vami-debug-system-timezone-timezonelist-item470" },
            { value: "Pacific/Yap", id: "vami-debug-system-timezone-timezonelist-item471" },
            { value: "Poland", id: "vami-debug-system-timezone-timezonelist-item472" },
            { value: "Portugal", id: "vami-debug-system-timezone-timezonelist-item473" },
            { value: "Singapore", id: "vami-debug-system-timezone-timezonelist-item474" },
            { value: "US/Alaska", id: "vami-debug-system-timezone-timezonelist-item475" },
            { value: "US/Aleutian", id: "vami-debug-system-timezone-timezonelist-item476" },
            { value: "US/Arizona", id: "vami-debug-system-timezone-timezonelist-item477" },
            { value: "US/Central", id: "vami-debug-system-timezone-timezonelist-item478" },
            { value: "US/Eastern", id: "vami-debug-system-timezone-timezonelist-item479" },
            { value: "US/East-Indiana", id: "vami-debug-system-timezone-timezonelist-item480" },
            { value: "US/Hawaii", id: "vami-debug-system-timezone-timezonelist-item481" },
            { value: "US/Indiana-Starke", id: "vami-debug-system-timezone-timezonelist-item482" },
            { value: "US/Michigan", id: "vami-debug-system-timezone-timezonelist-item483" },
            { value: "US/Mountain", id: "vami-debug-system-timezone-timezonelist-item484" },
            { value: "US/Pacific", id: "vami-debug-system-timezone-timezonelist-item485" },
            { value: "US/Samoa", id: "vami-debug-system-timezone-timezonelist-item486" },
            { value: "UTC", id: "vami-debug-system-timezone-timezonelist-item487" },
            { value: "WET", id: "vami-debug-system-timezone-timezonelist-item488" },
            { value: "Zulu", id: "vami-debug-system-timezone-timezonelist-item489" }
        ];

        $scope.selectedTimeZone = $scope.timeZones[1];

        var getTimeZoneMessage = {
            type: "TimeZone",
            operation: "get"
        };

        var setTimeZoneMessage = {
            type: "TimeZone",
            operation: "setTimeZone",
            data: {
                newTimeZone: $scope.timeZones[1]
            }
        };

        var setTimeZoneTestMessage = {
            type: "TimeZone",
            operation: "testSetTimeZone",
            data: {
                newTimeZone: $scope.timeZones[1]
            }
        };

        $scope.getTimeZone();
    }]);
