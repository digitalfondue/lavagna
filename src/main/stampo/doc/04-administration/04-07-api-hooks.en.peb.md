## External integrations

You can add customized javascript programs to react on application events.

It can be useful for example to notify an external system, like a slack channel.


### Script creation

The script receive the following global variables:

 - `log`: an instance of a Logger
 - `GSON`: an instance of Gson
 - `restTemplate`: an instance of a RestTemplate
 - `eventName`: a string with the current triggered event. See below the values
 - `project`: the project name (string)
 - `user`: an instance of io.lavagna.model.apihook.User: the user that triggered the event
 - `configuration`: a map containing configuration values
 - `data`: a map containing additional values

#### Supported events

 - CREATE_PROJECT
 - UPDATE_PROJECT
 - CREATE_BOARD, additional global variables:
    - `board`: short name of the board (string)
 - UPDATE_BOARD, additional global variables:
    - `board`: short name of the board (string)
 - CREATE_COLUMN, additional global variables:
    - `board`: short name of the board (string)
    - `columnName`: column name (string)
 - UPDATE_COLUMN, additional global variables:
    - `board`: short name of the board (string)
    - `previous`: previous column (io.lavagna.model.apihook.Column)
    - `updated`: current column (io.lavagna.model.apihook.Column)
 - CREATE_CARD, additional global variables:
    - `board`: short name of the board (string)
    - `card`: newly created card (io.lavagna.model.apihook.Card)
 - UPDATE_CARD, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `previous`: previous name (string)
    - `updated`: current name (string)
 - UPDATE_DESCRIPTION, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `previous`: previous card data (io.lavagna.model.apihook.Card)
    - `updated`: current card data (io.lavagna.model.apihook.Card) 
 - CREATE_COMMENT
 - UPDATE_COMMENT
 - DELETE_COMMENT
 - UNDO_DELETE_COMMENT
 - CREATE_ACTION_LIST
 - DELETE_ACTION_LIST
 - UPDATE_ACTION_LIST
 - CREATE_ACTION_ITEM
 - DELETE_ACTION_ITEM
 - TOGGLE_ACTION_ITEM
 - UPDATE_ACTION_ITEM
 - MOVE_ACTION_ITEM
 - UNDO_DELETE_ACTION_ITEM
 - UNDO_DELETE_ACTION_LIST
 - CREATE_FILE
 - DELETE_FILE
 - UNDO_DELETE_FILE
 - REMOVE_LABEL_VALUE
 - ADD_LABEL_VALUE_TO_CARD

### Script example

The following script create post on a slack channel:

```
log.warn("eventName: " + eventName + ", project: " + project + ", 
    data: " + GSON.toJson(data));
var userName = user.displayName || user.username;

function formatCard(card) {
  return card.boardShortName + '-' + card.sequence + ' "' + card.name + '" ' + card.url;
}

function formatColumn(column) {
  return column.name;
}

var supportedEvents = {
  CREATE_CARD: function() {
    return {text: 'User "' + userName+'" has created the card: ' + formatCard(data.card)};
  },
  UPDATE_CARD_POSITION: function() {
    var moreThanOne = data.affectedCards.length > 1;
    var cardText = 'card'+(moreThanOne ? 's' : '');
    for(var i = 0; i < data.affectedCards.length; i++) {
      cardText += ' '+formatCard(data.affectedCards[i]);
    }
    return {text: 'User "' + userName+'" moved the ' + cardText + 
        ' from column ' + formatColumn(data.from) + ' to column ' + formatColumn(data.to)};
  },
  CREATE_COMMENT: function() {
    return {text: 'User "' + userName+'" has posted the comment: ' 
        +   data.comment.content  + ' in the card ' + formatCard(data.card)};
  },
  UPDATE_COMMENT: function() {
    return {text: 'User "' + userName+'" has updated a comment to' 
        + data.updated +' in the card ' + formatCard(data.card)}
  },
  UPDATE_DESCRIPTION: function() {
    return {text: 'User "' + userName+'" has updated the card description to ' 
        + data.updated.content +' in the card ' + formatCard(data.card)}
  }
};

if(supportedEvents[eventName]) {
  var payload = supportedEvents[eventName]();
  if(payload) {
    restTemplate.postForLocation('https://hooks.slack.com/services/.../.../...', payload);
  }
}
```

We check if the event is handled by our script by using a map of string, functions. 
If the event is present in the map, we execute the function.
