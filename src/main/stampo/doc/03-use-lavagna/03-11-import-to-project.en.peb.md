## Import data from external sources

### Import data from Trello

If a Trello API key has ben configured then you can import any board directly into Lavagna.
To configure the Trello API key look at the <a href="{{relativeRootPath}}/03-configuration-and-administration/03-01-config-parameters.html">Configuration Parameters</a> documentation.
Please note that currently it will import only some part of your boards (columns, cards, comments, checklists, members, due date) and the creator will be the user used for the import.

First to enable the connection to trello you will need to click the "Load Trello connector": on success the "Connect to Trello" button will appear:

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_trello_import.png" alt="Trello import">

On connect you'll be asked to authorize Lavagna.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_trello_authorize.png" alt="Trello authorize">

After the authorization you can choose which board you want to import.
By enabling "Import also archived cards" you'll import also the board's archive, be advised that this may severely impact the amount of data processed.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_trello_import_options.png" alt="Trello import options">

Once you configured what you want to copy the cards are imported in Lavagna.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_trello_progress.png" alt="Trello import progress bar">

At the end of the process you can return on the Project's home and see the newly created boards.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_trello_done_project.png" alt="Trello imported project">

And by clicking on the board you can see the cards.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_trello_done_board.png" alt="Trello imported board">

As reference, this is the original board copied from Trello.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_trello_original_board.png" alt="Trello imported board">
