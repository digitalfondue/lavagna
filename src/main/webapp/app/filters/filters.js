(function () {
    'use strict';

    var filters = angular.module('lavagna.filters');

    var renderer = new marked.Renderer();

    // render images as link
    renderer.image = function (href, title, text) {
        return renderer.link(href, title || text || href, text || title || href);
    };

    var origLinkFn = renderer.link;

    renderer.link = function (href, title, text) {
        var link = origLinkFn.apply(renderer, [href, title, text]);

        if (link.indexOf('<a') === 0) {
            return link.replace(/^<a/, '<a target="_blank" rel="noopener noreferrer"');
        } else {
            return link;
        }
    };

    marked.setOptions({
        breaks: true,
        sanitize: true,
        highlight: function (code) {
            return window.hljs.highlightAuto(code).value;
        }
    });

    function isEmpty(text) {
        return text === undefined || text === null || text === '';
    }

    function decorateCache(funToDecorate, cachedCount, name, $cacheFactory) {
        var cache = $cacheFactory(name, {number: cachedCount});

        return function cacheDecorator(key) {
            var res = cache.get(key);

            if (res !== undefined) {
                return res;
            }

            res = funToDecorate.apply(this, arguments); /* eslint no-invalid-this: 0 */

            cache.put(key, res);

            return res;
        };
    }

    filters.filter('markdown', function ($cacheFactory) {
        return decorateCache(function (text) {
            if (isEmpty(text)) {
                return '';
            }

            return marked(text, {renderer: renderer});
        }, 1000, 'markdown', $cacheFactory);
    });

    filters.filter('daysDiff', function () {
        return function daysDiffFilter(input) {
            if (isEmpty(input)) {
                return null;
            }

            return moment().startOf('day').diff(input, 'days');
        };
    });

    filters.filter('capitalize', function () {
        return function capitalizeFilter(input) {
            if (isEmpty(input)) {
                return null;
            }
            input = input.toLowerCase();

            return input.substring(0, 1).toUpperCase() + input.substring(1);
        };
    });

    // -----------------------------

    filters.filter('formatUser', function () {
        return function formatUserFilter(user) {
            if (user === undefined) {
                return null;
            }
            if (!isEmpty(user.displayName) && user.displayName.trim().length > 0) {
                return user.displayName;
            }

            return user.username;
        };
    });

    filters.filter('userInitials', function () {
        return function userInitialsFilter(user) {
            if (user === undefined) {
                return null;
            }
            if (!isEmpty(user.displayName)) {
                return user.displayName.charAt(0).toUpperCase();
            }

            return user.username.charAt(0).toUpperCase();
        };
    });

    filters.filter('dateIncremental', function ($filter, $cacheFactory) {
        return decorateCache(function (v) {
            if (v === undefined || v === null) {
                return '';
            }

            var date = new Date();
            var year = date.getFullYear();
            var dateYear = v.substring(0, 4);

            if (year === dateYear) {
                var day = date.getDate();
                var month = date.getMonth() + 1;
                var dateDay = $filter('date')(v, 'd');
                var dateMonth = $filter('date')(v, 'M');

                if (day === dateDay && month === dateMonth) {
                    return $filter('date')(v, 'h:mm a');
                } else {
                    return $filter('date')(v, 'MMM d h:mm a');
                }
            } else {
                return $filter('date')(v, 'MMM d, y h:mm a');
            }
        }, 1000, 'dateIncremental', $cacheFactory);
    });

    function textColorFromBg(color) {
        // using the formula described here : https://harthur.github.io/brain/
        var r = (color >> 16 & 0xff),
            g = (color >> 8 & 0xff),
            b = (color & 0xff);
        var yiq = (r * 299 + g * 587 + b * 114) / 1000;

        return (yiq >= 160) ? 'black' : 'white';
    }

    filters.filter('labelBackground', function ($filter, $cacheFactory) {
        return decorateCache(function (color) {
            if (color === undefined || color === null) {
                return '';
            }

            var lpad = function (value, padding) {
                var zeroes = '0';

                for (var i = 0; i < padding; i++) {
                    zeroes += '0';
                }

                return (zeroes + value).slice(padding * -1);
            };

            return {'background-color': '#' + lpad(color.toString(16), 6)};
        }, 100, 'labelBackground', $cacheFactory);
    });

    function toRGBAColor(num, alpha) {
        num >>>= 0;
        var b = num & 0xFF,
            g = (num & 0xFF00) >>> 8,
            r = (num & 0xFF0000) >>> 16,
            a = alpha;

        return 'rgba(' + [r, g, b, a].join(',') + ')';
    }

    filters.filter('labelChartBar', [function () {
        return function (label, max) {
            var style = {'background-color': toRGBAColor(label.labelColor, 0.6) };

            style.border = '1px solid ' + toRGBAColor(label.labelColor, 1.0);
            style.width = (100 * label.count / max) + '%';

            return style;
        };
    }]);

    filters.filter('labelBackgroundClass', [function () {
        return function (color) {
            return textColorFromBg(color);
        };
    }]);

    filters.filter('columnColor', function ($filter, $cacheFactory) {
        return decorateCache(function (color) {
            if (color === undefined) {
                color = 0;
            }

            var lpad = function (value, padding) {
                var zeroes = '0';

                for (var i = 0; i < padding; i++) {
                    zeroes += '0';
                }

                return (zeroes + value).slice(padding * -1);
            };

            return {'border-top': '2px solid #' + lpad(color.toString(16), 6)};
        }, 100, 'columnColor', $cacheFactory);
    });

    filters.filter('color', function ($filter, $cacheFactory) {
        return decorateCache(function (color) {
            if (color === undefined) {
                color = 0;
            }

            var lpad = function (value, padding) {
                var zeroes = '0';

                for (var i = 0; i < padding; i++) {
                    zeroes += '0';
                }

                return (zeroes + value).slice(padding * -1);
            };

            return {'color': '#' + lpad(color.toString(16), 6)};
        }, 100, 'color', $cacheFactory);
    });

    filters.filter('fileBytes', function () {
        return function (bytes, precision) {
            if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) { return '-'; }
            if (typeof precision === 'undefined') { precision = 1; }
            var units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB'],
                number = Math.floor(Math.log(bytes) / Math.log(1024));

            return (bytes / Math.pow(1024, Math.floor(number))).toFixed(precision) + ' ' + units[number];
        };
    });

    filters.filter('uniqueLabels', function () {
        return function (userLabels, labelValues) {
            var filteredLabels = {};

            for (var k in userLabels) {
                if (labelValues === undefined || !userLabels[k].unique || !(userLabels[k].id in labelValues)) {
                    filteredLabels[k] = userLabels[k];
                }
            }

            return filteredLabels;
        };
    });

    /* expecting dueDate with format: dd.MM.yyyy and dueDateTime with format HH:mm */
    filters.filter('extractISO8601Date', function ($filter) {
        return function (dueDate, dueDateTime) {
            if (dueDate instanceof Date) {
                return $filter('date')(dueDate, 'yyyy-MM-ddTHH:mm:ss.sssZ');
            }

            try {
                var isDueDateTimeUndefined = dueDateTime === undefined;

                dueDateTime = dueDateTime || '12:00';

                var ddmmyyyy = dueDate.trim().split('.');
                var hhmm = dueDateTime.trim().split(':');

                if (ddmmyyyy.length !== 3 || hhmm.length !== 2) {
                    return false;
                }

                var date;

                if (isDueDateTimeUndefined) {
                    date = new Date(ddmmyyyy[2] + '-' + ddmmyyyy[1] + '-' + ddmmyyyy[0] + 'T' + hhmm[0] + ':' + hhmm[1] + ':00+00:00');
                } else {
                    date = new Date(parseInt(ddmmyyyy[2], 10), parseInt(ddmmyyyy[1], 10) - 1, parseInt(ddmmyyyy[0], 10), parseInt(hhmm[0], 10), parseInt(hhmm[1], 10), 0, 0);
                }

                if (isNaN(date.getTime())) {
                    return false;
                }

                return $filter('date')(date, 'yyyy-MM-ddTHH:mm:ss.sssZ');
            } catch (e) {
                return false;
            }
        };
    });

    filters.filter('parseHexColor', [function () {
        return function (hexColor) {
            if (hexColor === undefined || hexColor === null) {
                return 0;
            }
            var r = parseInt(hexColor.trim().replace('#', ''), 16);

            return isNaN(r) ? 0 : r;
        };
    }]);

    filters.filter('parseIntColor', [function () {
        return function (intColor) {
            if (isNaN(parseInt(intColor))) {
                return '#000000';
            }

            return '#000000'.substr(0, 7 - intColor.toString(16).length) + intColor.toString(16);
        };
    }]);

    filters.filter('addColDefOrder', [function () {
        var columnDefinitionOrder = { 'open': 0, 'closed': 1, 'backlog': 2, 'deferred': 3 };

        return function (definitions) {
            var r = [];

            if (definitions === undefined || definitions === null) {
                return r;
            }

            // handle map form
            if (definitions.length === undefined) {
                angular.forEach(definitions, function (val, key) {
                    val.order = columnDefinitionOrder[key.toLowerCase()];
                    r.push(val);
                });

                return r;
            }

            for (var d = 0; d < definitions.length; d++) {
                var definition = definitions[d];

                definition.order = columnDefinitionOrder[definition.value.toLowerCase()];
                r.push(definition);
            }

            return r;
        };
    }]);

    filters.filter('permissionsByCategory', [function () {
        var validCategories = ['APPLICATION', 'PROJECT', 'BOARD', 'COLUMN', 'CARD'];

        return function (permissions, category) {
            var r = [];

            if (permissions === undefined || permissions === null || permissions === undefined || permissions === null ||
                validCategories.indexOf(category) === -1) {
                return r;
            }
            for (var name in permissions) {
                if (name === category) {
                    r = permissions[name];
                }
            }

            return r;
        };
    }]);

    filters.filter('orderObjectBy', function () {
        return function (items, field, reverse) {
            var filtered = [];

            angular.forEach(items, function (item) {
                filtered.push(item);
            });
            filtered.sort(function (a, b) {
                if (a[field] > b[field]) { return 1; }
                if (a[field] < b[field]) { return -1; }

                return 0;
            });
            if (reverse) { filtered.reverse(); }

            return filtered;
        };
    });

    filters.filter('mask', [function () {
        return function (text) {
            if (text === undefined) {
                return null;
            }

            return text.replace(/./gi, '*');
        };
    }]);

    filters.filter('translateColumnName', function ($filter) {
        return function (column) {
            if (column === undefined || column === null) {
                return null;
            }

            if (column.location === 'BOARD') {
                return $filter('translate')('card.metadata.status.view', {name: column.name});
            } else {
                var locationName = $filter('translate')('common.location.definition.' + column.name);

                return $filter('translate')('card.metadata.status.view', {name: locationName});
            }
        };
    });

    filters.filter('filterUsersBy', function () {
        return function (users, text) {
            if (text === undefined || text === null || text.length === 0 || users === undefined) {
                return users;
            }

            var filteredUsers = [];

            angular.forEach(users, function (user) {
                var add = false;

                if (user.username.indexOf(text) >= 0) {
                    add = true;
                }
                if (!add && user.provider.indexOf(text) >= 0) {
                    add = true;
                }
                if (!add && !isEmpty(user.displayName) && user.displayName.indexOf(text) >= 0) {
                    add = true;
                }
                if (!add && !isEmpty(user.email) && user.email.indexOf(text) >= 0) {
                    add = true;
                }
                if (add) {
                    filteredUsers.push(user);
                }
            });

            return filteredUsers;
        };
    });

    // imported from https://github.com/angular/angular.js/blob/master/src/ng/filter/limitTo.js
    // which is under the following license:
    /*
The MIT License

Copyright (c) 2010-2015 Google, Inc. http://angularjs.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
     */
    filters.filter('limitToWithOffset', function limitToWithOffset() {
        function toInt(str) {
            return parseInt(str, 10);
        }

        return function (input, limit, begin) {
            if (Math.abs(Number(limit)) === Infinity) {
                limit = Number(limit);
            } else {
                limit = toInt(limit);
            }
            if (isNaN(limit)) { return input; }

            if (angular.isNumber(input)) { input = input.toString(); }
            if (!angular.isArray(input) && !angular.isString(input)) { return input; }

            begin = (!begin || isNaN(begin)) ? 0 : toInt(begin);
            begin = (begin < 0 && begin >= -input.length) ? input.length + begin : begin;

            if (limit >= 0) {
                return input.slice(begin, begin + limit);
            } else if (begin === 0) {
                return input.slice(limit, input.length);
            } else {
                return input.slice(Math.max(0, begin + limit), begin);
            }
        };
    });
}());
