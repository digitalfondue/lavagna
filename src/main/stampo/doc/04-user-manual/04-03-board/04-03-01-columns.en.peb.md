### Columns

Columns are used to define the current status of a card. This is done by combining two metadata:

* the name of the column
* the status assigned to the column, represented by the color in the header

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_board_column.png" alt="Column">

There are four main statuses:

* OPEN: used for both new and in progress cards, without distinction
* CLOSED
* BACKLOG
* DEFERRED

The aggregate number of cards by status present in the board is then reflected in the main and project dashboard.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_board_panel.png" alt="Board overview">

#### Create a new column

To create a new column scroll on the rightmost side of the board to the new column panel, enter the column name, pick a column status, and click **Add**.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_board_new-column.png" alt="Add a new column">

#### Change the name of a column

To change the name of a column, click on the name itself to open the edit form. Enter the new name, and then press either update to save the change, or cancel to discard it.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_board_name-edit.png" alt="Change the name of a column">

#### Column menu

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_board_column-menu.png" alt="Column menu">

Each column has its own context menu, reachable from the <i class="fa fa-chevron-down"></i> icon that appears when the cursor moves on the column header, containing a small set of useful operations:

* **Add a card**: see add a card
* **Status**: change the status assigned to the column
* **Move all cards to Archive**: archive all the cards that belong to the column, but keep the column in the board
* **Move all cards to Backlog**: move all the cards that belong to the column to the board backlog, but keep the column in the board
* **Move all cards to Trash**: move all the cards that belong to the column to the board trash bin, but keep the column in the board
* **Move column to Archive**: archive the column and all its cards. The column won't be available in the board anymore
* **Move column to Backlog**: move the column and all its cards to the board backlog. The column won't be available in the board anymore
* **Move column to Trash**: move the column and all its cards to the board trash bin. The column won't be available in the board anymore
