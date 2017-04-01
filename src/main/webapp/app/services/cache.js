(function () {
    'use strict';

    var services = angular.module('lavagna.services');

    services.factory('BaseCache', function (EventBus) {
        return {
            removeFromCacheAndEmit: function (id, cache, emitPrefix) {
                if (id in cache) {
                    delete cache[id];
                }
                EventBus.emit(emitPrefix + id, {});
            },
            parseEventAndEmitUpdate: function (message, cache, emitPrefix) {
                var id = JSON.parse(message.body)['payload'];

                this.removeFromCacheAndEmit(id, cache, emitPrefix);
            }
        };
    });

    services.factory('UserCache', function (StompClient, BaseCache, User) {
        var userCache = {};

        StompClient.subscribe('/event/user', function (message) {
            BaseCache.parseEventAndEmitUpdate(message, userCache, 'refreshUserCache-');
        });

        return {
            user: function (userId) {
                if (!(userId in userCache)) {
                    userCache[userId] = User.user(userId);
                }

                return userCache[userId];
            }
        };
    });

    services.factory('ProjectCache', function (StompClient, BaseCache, Project) {
        var projectCache = {};

        StompClient.subscribe('/event/project', function (message) {
            BaseCache.parseEventAndEmitUpdate(message, projectCache, 'refreshProjectCache-');
        });

        return {
            project: function (shortName) {
                if (!(shortName in projectCache)) {
                    projectCache[shortName] = Project.findByShortName(shortName);
                }

                return projectCache[shortName];
            },

            metadata: function (shortName) {
                return Project.getMetadata(shortName);
            }
        };
    });

    services.factory('BoardCache', function (StompClient, BaseCache, Board) {
        var boardCache = {};
        var columnCache = {};

        return {
            board: function (shortName) {
                if (!(shortName in boardCache)) {
                    boardCache[shortName] = Board.findByShortName(shortName);
                    StompClient.subscribe('/event/board/' + shortName, function () {
                        BaseCache.removeFromCacheAndEmit(shortName, boardCache, 'refreshBoardCache-');
                    });
                }

                return boardCache[shortName];
            },
            column: function (columnId) {
                if (!(columnId in columnCache)) {
                    columnCache[columnId] = Board.column(columnId);
                }

                return columnCache[columnId];
            }
        };
    });

    services.factory('CardCache', function (StompClient, BaseCache, Card) {
        var cardDataCache = {};

        var cardCache = {};
        var cardSeqToId = {};
        var subscribedBoardCards = {};

        var addCardToCache = function (card) {
            return card.then(function (c) {
                cardCache[c.id] = card;
                cardSeqToId[c.boardShortName + '-' + c.sequence] = c.id;
                var path = '/event/' + c.projectShortName + '/' + c.boardShortName + '/card';

                if (!(path in subscribedBoardCards)) {
                    StompClient.subscribe(path, function (message) {
                        BaseCache.parseEventAndEmitUpdate(message, cardCache, 'refreshCardCache-');
                    });
                    subscribedBoardCards[path] = true;
                }

                return card;
            });
        };

        return {
            card: function (cardId, invalidate) {
                if (invalidate || !(cardId in cardCache)) {
                    return addCardToCache(Card.findCardById(cardId));
                }

                return cardCache[cardId];
            },
            cardByBoardShortNameAndSeqNr: function (shortName, seqNr) {
                var key = shortName + '-' + seqNr;

                if (key in cardSeqToId) {
                    var cardId = cardSeqToId[key];

                    if (cardId in cardCache) {
                        return cardCache[cardId];
                    }
                }
                var card = Card.findCardByBoardShortNameAndSeqNr(shortName, seqNr);

                addCardToCache(card);

                return card;
            },
            cardData: function (id) {
                if (!(id in cardDataCache)) {
                    cardDataCache[id] = Card.activityData(id);
                }

                return cardDataCache[id];
            }
        };
    });

    services.factory('LabelCache', function (StompClient, BaseCache, Label) {
        var labelListValues = {};
        var labelsByProjectCache = {};
        var subscribedProjects = {};

        return {
            findByProjectShortName: function (shortName) {
                if (!(shortName in labelsByProjectCache)) {
                    labelsByProjectCache[shortName] = Label.findByProjectShortName(shortName);
                    var path = '/event/project/' + shortName + '/label';

                    if (!(path in subscribedProjects)) {
                        StompClient.subscribe(path, function () {
                            // TODO clear only the labels associated to the subscribed project
                            labelListValues = {};
                            BaseCache.removeFromCacheAndEmit(shortName, labelsByProjectCache, 'refreshLabelCache-');
                        });
                        subscribedProjects[path] = true;
                    }
                }

                return labelsByProjectCache[shortName];
            },

            findLabelByProjectShortNameAndId: function (shortName, labelId) {
                return this.findByProjectShortName(shortName).then(function (data) {
                    return data[labelId];
                });
            },

            findLabelListValues: function (labelId) {
                if (!(labelId in labelListValues)) {
                    labelListValues[labelId] = Label.findLabelListValues(labelId);
                }

                return labelListValues[labelId];
            },
            findLabelListValue: function (labelId, listValueId) {
                return this.findLabelListValues(labelId).then(function (values) {
                    for (var i in values) {
                        if (values[i].id === listValueId) {
                            return values[i];
                        }
                    }

                    return null;
                });
            }
        };
    });
}());
