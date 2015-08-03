## Search

The search bar at the top is context sensitive. The context are:

 - global search: when the user is in the home page
 - project specific search: when the user is in a project
 - board filter: when the user is in the board view
 
Currently the search only support implicitly the "AND" operator. Each search term are
part of a list of filter that *must* match.


### Filters

In both the global/project and board filter the following filters can be defined. For most filters, only a single value can be specified. The filters that receive dates as a parameter can receive two dates for defining a time interval.

#### label

A label filter begin with the hash symbol: '#'.

When entering # in the search bar, a list of possible label is displayed. For example:

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_search_autocomplete_label_suggestion.png" alt="Label suggestions">


When confirming the filter, the search will be done. In this case all the cards with the label "Type" are shown, regardless of the associated value.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_search_autocomplete_label_type.png" alt="Label type search">


For searching the associated value, the complete syntax is : #LABEL: ASSOCIATED_VALUE, in the screenshot below it can be seen in action:

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_search_autocomplete_label_type_bugfixing.png" alt="Label type with bugfixing value search">

#### to

For searching the cards assigned (or not) to a specific user, the "to: " filter must be used.

The following values are permitted:

 - **to:me**
 - **to:unassigned**
 - **to:LOGIN_PROVIDER:USERNAME**
 
The "to:me" show the cards assigned to the current user.

The "to:unassiged" show all the cards without an assigned user.

The "to:LOGIN_PROVIDER:USERNAME" show the cards assigned to a specific user. For example: "to:demo:user1".

#### by

"by" search all the cards created by the specified user.

The following values are permitted:

 - **by:me**
 - **by:LOGIN_PROVIDER:USERNAME**

Like the "to" filter, the "by:me" search the cards created by the current user and "by:LOGIN_PROVIDER:USERNAME" show the ones created by the specified user.

#### created

"created" show all the cards created in the specified time interval.

The supported values are:

 - **created:DATE**
 - **created:DATE1..DATE2**
 - **created:late**
 - **created:today**
 - **created:this week**
 - **created:this month**
 - **created:previous week**
 - **created:previous month**
 - **created:last week**
 - **created:last month**
 
The supported DATE format is yyyy-mm-dd. For searching an interval, add ".." between the two dates, for example: "created:2015-02-01..2015-10-01"

"created:late" will show all the cards created in the past.

#### watched

"watched" search all the cards watched (or notd) by a specific user.

The permitted values are:

 - **watched:me**
 - **watched:unassigned**
 - **watched:LOGIN_PROVIDER:USERNAME**
 
Like the "to" filter, the "watched:me" search all the cards watched by the current user, **watched:unassigned** search the one without watcher and finally "watched:LOGIN_PROVIDER:USERNAME" show the cards with the specified user.

#### updated

The "updated" filter show all the cards that have been updated in the specified time interval.

The following values are supported:

 - **updated:DATE**
 - **updated:DATE1..DATE2**
 - **updated:late**
 - **updated:today**
 - **updated:this week**
 - **updated:this month**
 - **updated:previous week**
 - **updated:previous month**
 - **updated:last week**
 - **updated:last month**

It follow exactly the same behaviour as the ["created"](#created) filter.

#### due

The "due" filter search all the cards that have the due date specified.

The following values are supported:

 - **updated:DATE**
 - **updated:DATE1..DATE2**
 - **updated:late**
 - **updated:today**
 - **updated:this week**
 - **updated:this month**
 - **updated:previous week**
 - **updated:previous month**
 - **updated:next week**
 - **updated:next month**
 - **updated:last week**
 - **updated:last month**

It follow exactly the same behaviour as the ["created"](#created) filter, in addition "next week" and "next month" are available as a shortcut. 

#### updated by

"updated_by" search all the cards updated by a specific user.

The following values are permitted:

 - **updated_by:me**
 - **updated_by:LOGIN_PROVIDER:USERNAME**

Like the "by" filter, the "updated_by:me" search the cards updated by the current user and "updated_by:LOGIN_PROVIDER:USERNAME" show the ones updated by the selected user.

#### milestone

For searching the cards that are assigned (or not) to a specific milestones, the **"milestone:"** filter must be used.

The accepted values are:

 - **milestone:unassigned**
 - **milestone:MILESTONE_NAME**

The "milestone:unassigned" search all the cards without an assigned milestone.

#### status

The **status:** filter allow to search the cards that are in a specific status (OPEN, CLOSED, BACKLOG, DEFERRED).

The following values are valid:

 - **status:OPEN**
 - **status:CLOSED**
 - **status:BACKLOG**
 - **status:DEFERRED** 

#### location

The **location:** filter search for the cards that are in a specific location (BOARD, ARCHIVE, BACKLOG, TRASH)

The following values are valid:

 - **location:BOARD**
 - **location:ARCHIVE**
 - **location:BACKLOG**
 - **location:TRASH** 

#### Free text search

All the text that don't fall in the others filters is considered a "free text search". At the moment it use the functions from the underlying DB. 


### Single board search

In the board view, the search bar generate a client side filter that is continuously applied when the cards are updated like the following screenshot:

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_search_board_search.png" alt="Board search">

Note: in this view, the "full text search" will not work as expected.


### Global and project specific search

When the search is triggered in the home page, a global search is done in all the projects that the user has access to. "Global search found" will be present, as depicted in the following screenshot:

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_search_global_search.png" alt="Global search">

When the user is in the **project** page, a project specific search will be done. In the search result, "Project Search Found" will be present:

<img class="pure-img" src="{{relativeRootPath}}/images/en/c04_search_project_search.png" alt="Project search">

For both search, some bulk operations can be done. Click the "select" menu, check the cards that need to be updated, like the [bulk operations in the board](04-03-board/04-03-05-bulk-operations.html)