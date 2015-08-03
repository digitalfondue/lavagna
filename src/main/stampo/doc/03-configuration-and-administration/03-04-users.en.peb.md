## Users

In this section, all the users created in Lavagna are listed.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c03_admin_users.png" alt="Users">

For each user, the following information are visible:

* **Account provider**: what provides the account and authenticates it.
* **Username**: the unique identifier for the user within the account provider, and the unique identified in Lavagna as well. Has to be unique.
* **Email**: the email address where to send notifications
* **Display name**: the name displayed around the application, doesn't have to be unique.
* **Enabled**: whether the user can or cannot use Lavagna

While the users are paginated by 20, it's also possible to filter the results. The search query will apply to provider, username, email, and displayName. A maximum of 10 results will be displayed.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c03_admin_users-filter.png" alt="Filter users">

### Create a user

To create a new user, click on **<i class="fa fa-plus"></i> Add user**, and fill the form with the right data.
All the roles available globally will be listed in the form.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c03_admin_add-user.png" alt="Filter users">

### Import users from a file

It is also possible to save time and import a large set of user from a JSON formatted file.

The format has to be:
<pre>
[
    {
        "provider" : "",
        "username" : "",
        "displayName" : "",
        "email" : "",
        "enabled" : true|false,
        "roles" : []
    },
    ...
]
</pre>

Only provider and username are mandatory at this point.

To then import the file, click on **<i class="fa fa-upload"></i> Import**, select the file, and click **Import**.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c03_admin_import-users.png" alt="Import users">

A notification will appear in the lower left corner with the result of the operation.