(function () {
    // FIXME REFACTOR
    'use strict';

    var parser = window.SEARCH_PARSER;

    function labelValueMatcher(criteria, environment) {
        var dateMatcherFn;
        var currentUserId;

        if (criteria.value === undefined || criteria.value === null) {
            return function () {
                return true;
            };
        }

        if (criteria.value.type === 'STRING') {
            var valueInt = parseInt(criteria.value.value, 10);
            var valueIsNaN = isNaN(valueInt);

            dateMatcherFn = function () {
                return true;
            };

            try {
                dateMatcherFn = dateMatcher(criteria);
            } catch (e) {
                // ignore
            }

            currentUserId = criteria.value.value.trim() === 'me' ? environment.currentUserId : undefined;

            return function (label) {
                if (label.labelType === 'STRING') {
                    return label.value.valueString.indexOf(criteria.value.value) === 0;
                } else if (label.labelType === 'INT' && !valueIsNaN) {
                    return label.value.valueInt === valueInt;
                } else if (label.labelType === 'TIMESTAMP') {
                    return dateMatcherFn(moment(label.value.valueTimestamp).toDate());
                } else if (label.labelType === 'USER' && currentUserId !== undefined) {
                    return label.value.valueUser === currentUserId;
                } else if (label.labelType === 'USER') {
                    return environment.users[criteria.value.value] === label.value.valueUser;
                } else if (label.labelType === 'CARD') {
                    return environment.cards[criteria.value.value] === label.value.valueCard;
                } else if (label.labelType === 'LIST') {
                    return environment.labelListValues[criteria.value.value] && environment.labelListValues[criteria.value.value][label.labelId] === label.labelValueList;
                }

                return true;
            };
        } else if (criteria.value.type === 'DATE_IDENTIFIER') {
            dateMatcherFn = dateMatcher(criteria);

            return function (label) {
                if (label.labelType === 'TIMESTAMP') {
                    return dateMatcherFn(moment(label.value.valueTimestamp).toDate());
                } else {
                    return false;
                }
            };
        } else if (criteria.value.type === 'CURRENT_USER' && criteria.value.value === 'me') {
            return function (label) {
                if (label.labelType === 'USER') {
                    return label.value.valueUser === environment.currentUserId;
                } else {
                    return true;
                }
            };
        }
    }

    function sameDay(d1, d2) {
        return d1.getDate() === d2.getDate() && d1.getMonth() === d2.getMonth() && d1.getFullYear() === d2.getFullYear();
    }

    function sameMonth(d1, d2) {
        return d1.getMonth() === d2.getMonth() && d1.getFullYear() === d2.getFullYear();
    }

    function dateInRange(d, r1, r2) {
        d.setHours(0, 0, 0, 0);

        return d >= r1 && d <= r2;
    }

    function dateInDays(d1, d2, days) {
        var timeDiff = Math.abs(d1.getTime() - d2.getTime());
        var diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24));

        return diffDays <= days;
    }

    function dateMatcher(criteria) {
        if (criteria.value.type === 'DATE_IDENTIFIER') {
            return {
                late: function () {
                    var now = new Date();

                    now.setHours(0, 0, 0, 0);

                    return function (date) {
                        date.setHours(0, 0, 0, 0);

                        return now > date;
                    };
                },
                today: function () {
                    var today = new Date();

                    return function (date) {
                        return sameDay(today, date);
                    };
                },
                yesterday: function () {
                    var yesterday = new Date();

                    yesterday.setDate(yesterday.getDate() - 1);

                    return function (date) {
                        return sameDay(yesterday, date);
                    };
                },
                tomorrow: function () {
                    var tomorrow = new Date();

                    tomorrow.setDate(tomorrow.getDate() + 1);

                    return function (date) {
                        return sameDay(tomorrow, date);
                    };
                },
                'this week': function () {
                    var today = new Date();

                    return function (date) {
                        return moment(today).isSame(date, 'week');
                    };
                },
                'this month': function () {
                    var today = new Date();

                    return function (date) {
                        return sameMonth(today, date);
                    };
                },
                'next week': function () {
                    return function (date) {
                        var nextWeek = moment().add(1, 'week');

                        return nextWeek.isSame(date, 'week');
                    };
                },
                'next month': function () {
                    var today = new Date();
                    var nextMonth = new Date(today.getFullYear(), today.getMonth() + 1, 1);

                    return function (date) {
                        return sameMonth(nextMonth, date);
                    };
                },
                'previous week': function () {
                    return function (date) {
                        var prevWeek = moment().add(-1, 'week');

                        return prevWeek.isSame(date, 'week');
                    };
                },
                'previous month': function () {
                    var today = new Date();
                    var previousMonth = new Date(today.getFullYear(), today.getMonth() - 1, 1);

                    return function (date) {
                        return sameMonth(previousMonth, date);
                    };
                },
                'last week': function () {
                    return function (date) {
                        return dateInDays(new Date(), date, 7);
                    };
                },
                'last month': function () {
                    return function (date) {
                        return dateInDays(new Date(), date, 30);
                    };
                }
            }[criteria.value.value]();
        } else {
            var dateString = criteria.value.value;
            var splitted = dateString.split('..');
            // FIXME: use user date parsing pref -> this is unambigous, but we must support MDY format too
            var acceptedFormat = ['D.M.YYYY', 'D-M-YYYY', 'D/M/YYYY', 'YYYY-M-D', 'YYYY.M.D', 'YYYY/M/D'];

            // string parser. Must support the following syntax: date -> for single date, date1..date2 -> for interval
            if (splitted.length === 2) {
                var parsed1 = moment(splitted[0].trim(), acceptedFormat, true);
                var parsed2 = moment(splitted[1].trim(), acceptedFormat, true);

                if (parsed1.isValid() && parsed2.isValid()) {
                    var r1 = parsed1.toDate();
                    var r2 = parsed2.toDate();

                    return function (date) {
                        return dateInRange(date, r1, r2);
                    };
                }
            } else {
                var parsedDate = moment(dateString.trim(), acceptedFormat, true);

                if (parsedDate.isValid()) {
                    var d = parsedDate.toDate();

                    return function (date) {
                        return sameDay(d, date);
                    };
                }
            }
        }
        throw 'invalid date format';
    }

    // from https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions
    function escapeRegExp(string) {
        return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'); // $& means the whole matched string
    }

    function buildMatcher(criteria, environment) {
        var currentUserId;
        var matchCurrentUserFn;
        var matchUserFn;

        if (criteria.type === 'USER_LABEL') {
            return function (card, labels) {
                var nameMatcher = criteria.name;

                var labelValFn = labelValueMatcher(criteria, environment);

                for (var i = 0; i < labels.length; i++) {
                    var label = labels[i];

                    if (label.labelName.indexOf(nameMatcher, 0) === 0 && labelValFn(label) && label.labelDomain === 'USER') {
                        return true;
                    }
                }

                return false;
            };
        } else if (criteria.type === 'DUE_DATE') {
            var dueDateMatchingFunction = dateMatcher(criteria);

            return function (card, labels) {
                for (var i = 0; i < labels.length; i++) {
                    var label = labels[i];

                    if (label.labelName === 'DUE_DATE' && label.labelDomain === 'SYSTEM') {
                        return dueDateMatchingFunction(moment(label.value.valueTimestamp).toDate());
                    }
                }

                return false;
            };
        } else if (criteria.type === 'CREATED') {
            var createdMatchingFunction = dateMatcher(criteria);

            return function (card) {
                return createdMatchingFunction(moment(card.creationDate).toDate());
            };
        } else if (criteria.type === 'MILESTONE') {
            if (criteria.value.type === 'UNASSIGNED') {
                return function (card, labels) {
                    for (var i = 0; i < labels.length; i++) {
                        var label = labels[i];

                        if (label.labelName === 'MILESTONE' && label.labelDomain === 'SYSTEM') {
                            return false;
                        }
                    }

                    return true;
                };
            } else {
                return function (card, labels) {
                    for (var i = 0; i < labels.length; i++) {
                        var label = labels[i];

                        if (label.labelName === 'MILESTONE' && label.labelDomain === 'SYSTEM') {
                            return environment.labelListValues[criteria.value.value] && environment.labelListValues[criteria.value.value][label.labelId] === label.labelValueList;
                        }
                    }

                    return false;
                };
            }
        } else if ((criteria.type === 'ASSIGNED' || criteria.type === 'WATCHED_BY')) {
            var labelName = criteria.type;

            //
            if (criteria.value.type === 'UNASSIGNED') {
                return function (card, labels) {
                    for (var i = 0; i < labels.length; i++) {
                        var label = labels[i];

                        if (label.labelName === labelName && label.labelDomain === 'SYSTEM') {
                            return false;
                        }
                    }

                    return true;
                };
            } else {
                currentUserId = environment.currentUserId;

                matchCurrentUserFn = function (label) {
                    return label.value.valueUser === currentUserId;
                };

                matchUserFn = function (label) {
                    return environment.users[criteria.value.value] === label.value.valueUser;
                };

                var matchingFunction = criteria.value.type === 'CURRENT_USER' ? matchCurrentUserFn : matchUserFn;

                return function (card, labels) {
                    var ret = false;

                    for (var i = 0; i < labels.length; i++) {
                        var label = labels[i];

                        if (label.labelName === labelName && label.labelDomain === 'SYSTEM') {
                            ret = ret || matchingFunction(label);
                        }
                    }

                    return ret;
                };
            }
        } else if (criteria.type === 'CREATED_BY') {
            currentUserId = environment.currentUserId;

            matchCurrentUserFn = function (card) {
                return card.creationUser === currentUserId;
            };

            matchUserFn = function (card) {
                return environment.users[criteria.value.value] === card.creationUser;
            };

            return criteria.value.type === 'CURRENT_USER' ? matchCurrentUserFn : matchUserFn;
        } else if (criteria.type === 'UPDATED_BY') {
            currentUserId = environment.currentUserId;

            matchCurrentUserFn = function (card) {
                return card.lastUpdateUserId === currentUserId;
            };

            matchUserFn = function (card) {
                return environment.users[criteria.value.value] === card.lastUpdateUserId;
            };

            return criteria.value.type === 'CURRENT_USER' ? matchCurrentUserFn : matchUserFn;
        } else if (criteria.type === 'UPDATED') {
            var matchingDateFunction = dateMatcher(criteria);

            return function (card) {
                return matchingDateFunction(moment(card.lastUpdateTime).toDate());
            };
        } else if (criteria.type === 'FREETEXT') {
            var matchRegex = new RegExp(escapeRegExp(criteria.value.value), 'i');
            var matchFreeText = function (card) {
                return card.name.match(matchRegex) !== null;
            };

            if (criteria.value.value.match(/[0-9]*/)) {
                return function (card) {
                    return card.sequence.toString().indexOf(criteria.value.value) === 0 || matchFreeText(card);
                };
            } else {
                return matchFreeText;
            }
        } else if (criteria.type === 'STATUS') {
            return function (card, label, columns) {
                var columnIdToMatch = card.columnId;
                var statusToMatch = criteria.value.value.toUpperCase();

                for (var i = 0; i < columns.length; i++) {
                    if (columns[i].id === columnIdToMatch && columns[i].status === statusToMatch) {
                        return true;
                    }
                }

                return false;
            };
        }

        //
        return function () {
            return true;
        };
    }

    function validUserFormat(value) {
        return value.split(':').length > 1;
    }

    function validCardFormat(value) {
        var r = value.split('-');

        return r.length > 1 && isFinite(parseInt(r[r.length - 1], 10));
    }

    // return promise
    function buildSearchFilter(criteria, columns, currentUserId, $http, $q) {
        if (criteria === undefined) {
            var deferred = $q.defer();

            deferred.resolve(function () {
                return true;
            });

            return deferred.promise;
        } else {
            var env = {currentUserId: currentUserId, usersToSearch: {}, cardsToSearch: {}, labelListValue: {}};
            var matchers = [];

            for (var i = 0; i < criteria.length; i++) {
                var crit = criteria[i];

                //
                if ((crit.value && crit.value.type === 'STRING') && (['ASSIGNED', 'CREATED_BY', 'WATCHED_BY', 'UPDATED_BY'].indexOf(crit.type) >= 0 ) && validUserFormat(crit.value.value)) {
                    env.usersToSearch[crit.value.value] = true;
                } else if (crit.value && crit.value.type === 'STRING' && crit.type === 'USER_LABEL') {
                    if (validUserFormat(crit.value.value)) {
                        env.usersToSearch[crit.value.value] = true;
                    }
                    if (validCardFormat(crit.value.value)) {
                        env.cardsToSearch[crit.value.value] = true;
                    }
                    env.labelListValue[crit.value.value] = true;
                } else if (crit.type === 'MILESTONE' && crit.value && crit.value.type === 'STRING') {
                    env.labelListValue[crit.value.value] = true;
                }
                //

                matchers.push(buildMatcher(crit, env));
            }

            var cardsToSearch = Object.getOwnPropertyNames(env.cardsToSearch);
            var usersToSearch = Object.getOwnPropertyNames(env.usersToSearch);
            var labelListValueToSearch = Object.getOwnPropertyNames(env.labelListValue);

            var searchMapping = function (values, apiToCall) {
                if (values.length > 0) {
                    return $http.get(apiToCall, {params: {from: values}}).then(function (res) {
                        return res.data;
                    });
                } else {
                    var deferred = $q.defer();

                    deferred.resolve({});

                    return deferred.promise;
                }
            };

            var cards = searchMapping(cardsToSearch, 'api/search/card-mapping');
            var users = searchMapping(usersToSearch, 'api/search/user-mapping');
            var labelListValues = searchMapping(labelListValueToSearch, 'api/search/label-list-value-mapping');

            return $q.all([cards, users, labelListValues]).then(function (res) {
                env.cards = res[0];
                env.users = res[1];
                env.labelListValues = res[2];

                return function (card) {
                    var matched = true;

                    for (var i = 0; i < matchers.length && matched; i++) {
                        matched = matched && matchers[i](card, card.labels, columns);
                    }

                    return matched;
                };
            });
        }
    }

    var extractData = function (data) {
        return data.data;
    };

    angular.module('lavagna.services').factory('Search', ['$http', '$q', function ($http, $q) {
        return {
            buildSearchFilter: function (criteria, columns, currentUserId) {
                return buildSearchFilter(criteria, columns, currentUserId, $http, $q);
            },
            parse: function (v) {
                var filtered = [];
                var res = parser.parse(v);

                for (var i = 0; i < res.length; i++) {
                    if (res[i].type !== 'WHITE_SPACE') {
                        filtered.push(res[i]);
                    }
                }

                return filtered;
            },
            autoCompleteCard: function (params) {
                return $http.get('api/search/autocomplete-card', {params: params}).then(extractData);
            }
        };
    }]);
}());
