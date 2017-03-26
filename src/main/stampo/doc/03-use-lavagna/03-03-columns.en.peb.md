## Columns

Columns are used to define the current status of a card. This is done by combining two metadata:

* the name of the column
* the status assigned to the column, represented by the color in the header

<img class="pure-img" src="{{relativeRootPath}}/images/en/column.png" alt="Column">

There are four main statuses:

* OPEN: used for both new and in progress cards, without distinction
* CLOSED
* BACKLOG
* DEFERRED

The aggregate number of cards by status present in the board is then reflected in the main and project dashboard.

<img class="pure-img" src="{{relativeRootPath}}/images/en/dashboard-panel.png" alt="Dashboard panel">

### Create a new column

To create a new column, click on the <span class="icon icon-add-column"></span> icon in the board left side bar.

A new dialog will appear: enter the column name, select a status, and click add.

<img class="pure-img" src="{{relativeRootPath}}/images/en/add-column.png" alt="Add column">

### Special columns

Each board has three special column:

* **BACKLOG**: A more general backlog column to free up space in the board. Cards in this column are still taken into the board count.
* **ARCHIVE**: If cards have been closed for a while, then it is advised to move them to the archive. Cards moved here are not included in the board count anymore.
* **TRASH**: Cards that have been created by mistake can be put here to separate them from the archive. Cards moved here are not included in the board count anymore.

#### Access the backlog

To access the backlog, click on the <span class="icon icon-board-backlog"></span> icon in the board left side bar.

#### Access the archive

To access the archive, click on the <span class="icon icon-board-archive"></span> icon in the board left side bar.

#### Access the trash

To access the trash, click on the <span class="icon icon-board-trash"></span> icon in the board left side bar.

### Change the name of a column

To change the name of a column, click on the name itself to open the edit form.

### Column menu

To open the column menu, click on the <span class="icon icon-more-vert"></span> icon in the column header.

The available operations are:

* **Select all**: Select all cards in the column
* **Select none**: Deselect all cards in the column
* **Column status**: change the status assigned to the column
* **Move all cards to Archive**: archive all the cards that belong to the column, but keep the column in the board
* **Move all cards to Backlog**: move all the cards that belong to the column to the board backlog, but keep the column in the board
* **Move all cards to Trash**: move all the cards that belong to the column to the board trash bin, but keep the column in the board
* **Move column to Archive**: archive the column and all its cards. The column won't be available in the board anymore
* **Move column to Backlog**: move the column and all its cards to the board backlog. The column won't be available in the board anymore
* **Move column to Trash**: move the column and all its cards to the board trash bin. The column won't be available in the board anymore

### Change status color

To modify the color representing each status, go to the [Project Settings](/03-use-lavagna/03-01-project.html#project-settings), and then the "Project" tab.

There, use the color picker to select a different color for each status.

<img class="pure-img" src="{{relativeRootPath}}/images/en/status-color.png" alt="Status color">


