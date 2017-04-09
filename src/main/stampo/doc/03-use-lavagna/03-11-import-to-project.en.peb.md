## Import data from external sources

To access the data import feature, go to the [Project Settings](/03-use-lavagna/03-01-project.html#project-settings), and then the "Import" tab.

### Trello

#### Requirements

Configure a Trello API Key in the [Configuration Parameters](/04-administration/04-01-config-parameters.html).

#### Import data

First, click "Load Trello Connector".

If the load is successful, the button will change to "Connect to Trello".

You will be required to allow Lavagna read-only access to your Trello account.

Once done, you will get a list of boards you can select to import.

When a board is selected, a short name will be automatically generated.

As a final option, you can select whether or not to import archived cards.

<img class="pure-img" src="{{relativeRootPath}}/images/en/import-from-trello.png" alt="Trello import">

When at least one board is selected, with a valid short name, the import process can start.

**Note**: Lavagna does not keep track of which boards have been imported.  That means you'll be able to import a board a second time, as long as a different short name is provided.
