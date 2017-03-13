### Manage labels

The labels are defined by the project. The labels are typed, the available types are:

 - "No Value": a simple label
 - Text: a custom text can be set by the user
 - Date: a date can be defined
 - Number: a number can be specified
 - Card: a card can be referenced (useful for a label like: "Duplicate of:")
 - User: a user can be referenced (useful for a label like: "Review:")
 - List: a value specified in a ordered list can be selected.

#### Creation

For adding a new label, the following form must be used:

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_project-admin_manage_labels_new.png" alt="New label">

The name (unique in the context of a project), the type, the color and whether the label must be unique in card must be specified.

The list values must be specified later, see the the [Update List section](#update-list).


#### Update

To update the name or color, click the <i class="fa fa-pencil" title="Edit"></i> icon.

##### Update List

To edit the list, click the <i class="fa fa-pencil" title="Edit"></i> icon and click on the "List Value" button. A modal view will appear to edit the list. 

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_project-admin_manage_labels_edit_list.png" alt="Edit label list">

The list can be sorted with the <i class="fa fa-arrow-up"></i> and <i class="fa fa-arrow-down"></i> buttons.


To remove a value, click the <i class="fa fa-trash-o"></i> button. If a value is currently used, it cannot be removed.

#### Delete

Only the labels that are _not_ used can be deleted. Click the <i class="fa fa-trash-o" title="Delete"></i> icon.
