## Login

The login section provide the configuration space for:

 - anonymous user access
 - login providers
 
 
### Anonymous user access

Anonymous access allows read-only access to the entire application. Users will be able to navigate projects, boards, and cards without the need to login in.

By default, the access for anonymous user is disabled.

<img class="pure-img" src="{{relativeRootPath}}/images/en/admin-login-anon.png" alt="Anonymous user access control">

Once enabled, it's possible to configure further settings:

* **Global Access**: give access to all project, regardless of each project anonymous access policy
* **Enable Search**: enable search for anonymous users

If only one of few projects should be accessible without login, enable anonymous access for each one individually.

To do that, access the "Anonymous Access" tab in the [Project Settings](/03-use-lavagna/03-01-project.html#project-settings).

### Login providers

Lavagna support multiple providers at the same time. In this section they can be enabled and configured.

#### Demo

The Demo provider is used for development and internal testing. Users belonging to the demo provider have their password automatically set as their username, without the possibility to change it.

<div class="alert-box"><b>ALERT</b>: don't use this provider for any purpose other than develpment or testing</div>

#### Password (internal provider)

The internal password provider is used for deployments that have no ability to connect to an external authentication provider, and enforce security via other means (e.g. access via VPN only).

Simply enable the password provider to start using it.

<div class="info-box"><b>NOTICE</b>: the Lavagna team still recomends to use a more secure provider, with features like two-factor authentication.</div>

#### Ldap

If the users are stored in a ldap directory (Active Directory is supported too), the ldap provider must be configured.

<img class="pure-img" src="{{relativeRootPath}}/images/en/admin-login-ldap.png" alt="Ldap provider">

It requires a user that can query the directory (the Manager DN and Manager Password). 

The query is composed by a base (Search base) and the filter (User search filter), where `{0}` is the placeholder for the username.

The configuration can be tested in the "Verify" form.

By checking "Enable automatic account creation", Lavagna will automatically create missing users, with DEFAULT role, if the LDAP authentication succeeds, but the user is not found internally.

#### Oauth 

The application support the following external oauth providers:
 
  - [bitbucket](https://bitbucket.org)
  - [gitlab](https://about.gitlab.com/gitlab-com/)
  - [github](https://github.com)
  - [google](https://google.com)
  - [twitter](https://twitter.com)
  
#### Preconfigured oauth providers

Select the oauth provider of the first account and provide the api key and secret. The provided callback url should be the correct one that must be provided.

See the documentation for:

 - [bitbucket](https://confluence.atlassian.com/display/BITBUCKET/OAuth+on+Bitbucket)
 - [gitlab](http://doc.gitlab.com/ce/integration/oauth_provider.html). Registration page is https://gitlab.com/profile/applications
 - [github](https://developer.github.com/v3/oauth/). Registration page is https://github.com/settings/applications/new
 - [google](https://developers.google.com/identity/protocols/OAuth2WebServer): the "Google+ API" must be enabled
 - [twitter](https://dev.twitter.com/web/sign-in/implementing)

#### Configurable oauth providers

Self-hosted gitlab instances can be configured with the "Add new provider" functionality.

Please note that if you are using **self signed certificates** you _must_ include them in the default keystore of your java virtual machine. See the [keytool](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html#keytool_option_importcert) documentation and this [stackoverflow post](http://stackoverflow.com/a/11617655). Lavagna will **not** provide a way to ignore untrusted certificates.

<img class="pure-img" src="{{relativeRootPath}}/images/en/admin-login-new-oauth.png" alt="New Oauth provider">

The new providers will appear in the section "Configurable OAuth providers".
