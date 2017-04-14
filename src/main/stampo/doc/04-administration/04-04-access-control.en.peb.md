## Access control

### Global and Project Roles

Lavagna supports two types of roles:

* **Global roles**: roles defined by the administrators. Those roles applies to every project.
* **Project roles**: roles defined by the project administrator. Those roles only apply to the project they belong.

#### Default roles

By default, Lavagna comes with two pre-defined roles:

* **ADMIN**: administration role, with all permissions, can't be deleted.
* **DEFAULT**: default user role, with no management permissions, can't be deleted.

#### Use roles for effective access control

The best way to prevent unwanted access to projects is to set up the roles on project level.

However, it is also good to keep in mind that the permissions for a few operations, like update profile and search, are defined in global roles.

To address that, create a global role that only allows search and update profile access.

Assign all users without administration rights to that role.

Then, ask the project administrators to set up their desired roles. Users won't be able to access a project until the project administrator assign them a role.

### Permissions

To each role belongs a list of permissions, describe in details in this section.

Global application permissions are:

* **Application Administration**: access the administration panel, and gives the ability to create new projects
* **Update user profile**: modify own user profile
* **Search**: access the search bar, and the search feature
* **Global API hook access**: API hook access for all the application
* **Project API hooks access**: API hook access limited to the project

Project permissions are:

* **Project_Administration**: access the project administration panel,and the ability to create new boards

Board permissions are:

* **Board access**: access boards in read only mode

Columns permissions are:

* **Create column**: create a new columns within a board
* **Move column**: ability to move a column around the board, including move the column to the archive, backlog, and trash
* **Rename column**: change the column name, and the status associated with the column

Card permissions are:

* **Create card**: create a new card
* **Update card**: change the card title, description, due date, milestone, watchers, and assigned users
* **Move card**: ability to move a card around the board, including to the ability to move the card to the archive, backlog, and trash
* **Create comment**: create a comment. In case of user own comment, it's also possible to update and delete
* **Update comment**: update another user comment
* **Delete comment**: delete another user comment
* **Action lists**: ability to create, modify, and delete action lists. This also applies to the action items within a list
* **Upload file**: upload a file
* **Update file**: not used in this version of lavagna, but reserved for future use cases
* **Delete file**: delete a file
* **Labels**: Add or remove labels on a card

### Manage roles

#### Add a new role

To add a role, click on the plus button, enter the role name, and click "Add".

By default, roles have no permissions associated.

#### Edit a role

<img class="pure-img" src="{{relativeRootPath}}/images/en/admin-manage-role.png" alt="Manage role">

To edit a role, click on the <span class="icon icon-edit"></span> icon.

A dialog with the list of available permissions will open. Select the desired one, and click "Update".

#### Delete a role

A role can be deleted when there are no users assigned to it.

To delete a role, click on the <span class="icon icon-delete"></span> icon.

#### Edit project role

To edit project roles, go to the [Project Settings](/03-use-lavagna/03-01-project.html#project-settings), and then the "Roles" tab.

Click the <span class="icon icon-edit"></span> icon on the role: the role's permissions dialog will open. Select the desired permissions, and press "Update".

<img class="pure-img" src="{{relativeRootPath}}/images/en/manage-project-edit-role.png" alt="Edit project role">
