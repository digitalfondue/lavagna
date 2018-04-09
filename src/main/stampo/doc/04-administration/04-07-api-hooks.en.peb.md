## External integrations

You can add customized javascript programs to react on application events.

It can be useful for example to notify an external system, like a slack channel.


### Script creation

To create a new integration, press the plus button. It will open the following modal window:

<img class="pure-img" src="{{relativeRootPath}}/images/en/integration-create.png" alt="create new integration modal window">

Insert the name, a description, configuration parameters (will be available in the global `configuration` variable) and the script.

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
 - UPDATE_CARD_POSITION, additional global variables:
    - `board`: short name of the board (string)
    - `affectedCards`: the cards that are moved (list of io.lavagna.model.apihook.Card)
    - `from`: the source column (io.lavagna.model.apihook.Column)
    - `to`: the destination column (io.lavagna.model.apihook.Column)
 - UPDATE_DESCRIPTION, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `previous`: previous card data (io.lavagna.model.apihook.Card)
    - `updated`: current card data (io.lavagna.model.apihook.Card) 
 - CREATE_COMMENT, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `comment`: the comment (io.lavagna.model.apihook.CardData)
 - UPDATE_COMMENT, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `previous`: previous comment (io.lavagna.model.apihook.CardData)
    - `updated`: current comment (io.lavagna.model.apihook.CardData)
 - DELETE_COMMENT, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `comment`: the deleted comment (io.lavagna.model.apihook.CardData)
 - UNDO_DELETE_COMMENT, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `comment`: the undeleted comment (io.lavagna.model.apihook.CardData)
 - CREATE_ACTION_LIST, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `actionList`: the name of the action list (string)
 - DELETE_ACTION_LIST, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `actionList`: the name of the action list (string)
 - UNDO_DELETE_ACTION_LIST, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `actionList`: the name of the action list (string)
 - UPDATE_ACTION_LIST, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `previous`: previous name of the list (string)
    - `updated`: current name of the list (string)
 - CREATE_ACTION_ITEM, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `actionList`: the name of the action list (string)
    - `actionItem`: the text of the item (string)
 - DELETE_ACTION_ITEM, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `actionList`: the name of the action list (string)
    - `actionItem`: the text of the item (string)
 - UNDO_DELETE_ACTION_ITEM, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `actionList`: the name of the action list (string)
    - `actionItem`: the text of the item (string)
 - TOGGLE_ACTION_ITEM, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `actionList`: the name of the action list (string)
    - `actionItem`: the text of the item (string)
    - `toggled`: the state of the action item (boolean) 
 - UPDATE_ACTION_ITEM, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `actionList`: the name of the action list (string)
    - `previous`: previous text (string)
    - `updated`: current text (string)
 - MOVE_ACTION_ITEM, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `actionItem`: the text of the item (string)
    - `from`: the name of the action list (string)
    - `to`:  the name of the action list (string)
 - CREATE_FILE, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `files`: the list of uploaded files (list of string)
 - DELETE_FILE, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `file`: the deleted file (string)
 - UNDO_DELETE_FILE, additional global variables:
    - `board`: short name of the board (string)
    - `card`: the card (io.lavagna.model.apihook.Card)
    - `file`: the undeleted file (string)
 - ADD_LABEL_VALUE_TO_CARD, additional global variables:
    - `board`: short name of the board (string)
    - `label`: the label (io.lavagna.model.apihook.Label)
    - `affectedCards`: the cards (list of io.lavagna.model.apihook.Card)
 - UPDATE_LABEL_VALUE, additional global variables:
    - `board`: short name of the board (string)
    - `label`: the label (io.lavagna.model.apihook.Label)
    - `affectedCards`: the cards (list of io.lavagna.model.apihook.Card)
 - REMOVE_LABEL_VALUE, additional global variables:
    - `board`: short name of the board (string)
    - `label`: the label (io.lavagna.model.apihook.Label)
    - `affectedCards`: the cards (list of io.lavagna.model.apihook.Card)

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


### Manage the integration

All the integrations will be listed as shown below.


<img class="pure-img" src="{{relativeRootPath}}/images/en/integration-manage.png" alt="manage integrations">

Clicking on the <span class="icon icon-disable-sync"></span> button, will disable the script, for re-enable it, click on <span class="icon icon-enable-sync"></span>.

You can edit (<span class="icon icon-edit"></span>) and delete (<span class="icon icon-delete"></span>) the integration too.
