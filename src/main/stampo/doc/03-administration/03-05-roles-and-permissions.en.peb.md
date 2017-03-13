## Roles and permissions

In this section, you can create new roles, manage the permissions of each role, and assign users to the desired roles.

Each roles contains a list permissions, that enable one or more actions. A user can be assigned to more than one role, and the permissions will be merged.

In addition, each project can have its own customized roles: users can be assigned a very restrictive global roles, and then a more permissive project roles.

Example: a project administrator doesn't have to be able to configure the application's SMTP settings, or access other projects. A global group with only **SEARCH** and **UPDATE_PROFILE** permissions will suffice, while at project level a group with all the permissions enabled will be assigned to him.

Lavagna ships with the global roles out of the box: **ADMIN** and **DEFAULT**. Those two roles cannot be deleted.

TBD global image

### Permissions

To each role belongs a list of permissions, describe in details in this section.

Global application permissions are:

* **ADMINISTRATION**: access the application administration panel, and the ability to create new projects
* **UPDATE_PROFILE**: update the user's own profile
* **SEARCH**: enable the search bar across the entire application

Project permissions are:

* **PROJECT_ADMINISTRATION**: access the project administration panel,and the ability to create new boards

Board permissions are:

* **READ**: access boards in read only mode

Columns permissions are:

* **CREATE_COLUMN**: create a new columns within a board
* **MOVE_COLUMN**: ability to move a column around the board, including to the ability to move the column to the archive, backlog, and trash
* **RENAME_COLUMNS**: change the column name, and the status associated with the column

Card permissions are:

* **CREATE_CARD**: create a new card
* **UPDATE_CARD**: change the card title, description, due date, milestone, watchers, and assigned users
* **MOVE_CARD**: ability to move a card around the board, including to the ability to move the card to the archive, backlog, and trash
* **CREATE_CARD_COMMENT**: create a comment. In case of user own comment, it's also possible to update and delete
* **UPDATE_CARD_COMMENT**: update another user comment
* **DELETE_CARD_COMMENT**: delete another user comment
* **MANAGE_ACTION_LIST**: ability to create, modify, and delete action lists. This also applies to the action items within a list
* **CREATE_FILE**: upload a file
* **UPDATE_FILE**: not used in this version of lavagna, but reserved for future use cases
* **DELETE_FILE**: delete a file
* **MANAGE_LABEL_VALUE**: create, delete, and update labels

### Manage roles

TBD create and edit
