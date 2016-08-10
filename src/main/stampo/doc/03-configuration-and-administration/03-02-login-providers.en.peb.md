## Login

The login section provide the configuration space for:

 - anonymous user access
 - login providers
 
 
### Anonymous user access

By default, the access for anonymous user is disabled. It can be enabled by clicking on the toggle at the right (see screenshot below):

<img class="pure-img" src="{{relativeRootPath}}/images/en/c03_admin_login_anonymous.png" alt="Anonymous user access configuration section">

As a standard behaviour, the anonymous user don't have access to any project. The access can be enabled globally, by toggling the "Global Access" option _or_ by enabling it in a per project basis in the [project configuration section](../04-user-manual/04-07-project-admin/04-07-03-manage-access.html#manage-access).

Additionally, the search functionality can be enabled for the anonymous users by toggling the "Enable Search" entry.

### Login providers

Lavagna support multiple providers at the same time. In this section they can be enabled and configured.


#### Demo

The demo provider must **not** be selected in production, as the password **is** the username. It can be useful for a small test round for evaluating the product.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c03_admin_login_demo.png" alt="Demo provider">

#### Ldap

If the users are stored in a ldap directory (Active Directory is supported too), the ldap provider must be configured.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c03_admin_login_ldap.png" alt="Ldap provider">

It requires a user that can query the directory (the Manager DN and Manager Password). 

The query is composed by a base (Search base) and the filter (User search filter), where `{0}` is the placeholder for the username.

The configuration can be tested in the "Verify" form.

#### Oauth 

The application support the following external oauth providers:
 
  - [bitbucket](https://bitbucket.org)
  - [gitlab](https://about.gitlab.com/gitlab-com/)
  - [github](https://github.com)
  - [google](https://google.com)
  - [twitter](https://twitter.com)
  
Additionally, self-hosted gitlab instances can be configured with the "Add new provider" functionality.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c03_admin_login_oauth.png" alt="Oauth provider">

#### Preconfigured oauth providers

Select the oauth provider of the first account and provide the api key and secret. The provided callback url should be the correct one that must be provided.

See the documentation for:

 - [bitbucket](https://confluence.atlassian.com/display/BITBUCKET/OAuth+on+Bitbucket)
 - [gitlab](http://doc.gitlab.com/ce/integration/oauth_provider.html). Registration page is https://gitlab.com/profile/applications
 - [github](https://developer.github.com/v3/oauth/). Registration page is https://github.com/settings/applications/new
 - [google](https://developers.google.com/identity/protocols/OAuth2WebServer): the "Google+ API" must be enabled
 - [twitter](https://dev.twitter.com/web/sign-in/implementing)

#### Configurable oauth providers

In the case of self-hosted oauth providers (currently only gitlab is supported), they can be configured by clicking the "Add new provider" button.  

Please note that if you are using **self signed certificates** you _must_ include them in the default keystore of your java virtual machine. See the [keytool](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html#keytool_option_importcert) documentation and this [stackoverflow post](http://stackoverflow.com/a/11617655). Lavagna will **not** provide a way to ignore untrusted certificates. 

It will open the following modal window:

<img class="pure-img" src="{{relativeRootPath}}/images/en/c03_admin_login_oauth_configurable.png" alt="Add new configurable Oauth provider modal window">

Select the type (currently only gitlab is available) and insert the name, the base url, the api key and secret. Copy the provided callback url in your oauth provider configuration screen. Save the provider by clicking the "Add new provider" button.

The new providers will appear in the section "Configurable OAuth providers", it can be :

 - updated by changing the values and clicking the "Save" button
 - deleted by clicking on the toggle and clicking the "Save" button
