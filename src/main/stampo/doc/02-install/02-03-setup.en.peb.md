## Setup

### Step 1, Base url or import

After successfully launching the application, go to http://your-lavagna-install/setup .

The following page will be shown:

<img class="pure-img" src="{{relativeRootPath}}/images/en/c02_install_step_1.png" alt="First setup step">

 - If it's a new install, confirm that the base url is correct and click **Next** 
 - If it's an import, select the exported file and click **Import**. The import process can take a noticeable amount of time.
 
### Step 2, Login provider configuration

Lavagna does **not** store the user credentials by design: an external provider must be chosen. There are 3 possible choice:

 - password
 - demo (use for test purpose only)
 - ldap
 - oauth
 
#### Password provider

If you want to manage your accounts inside lavagna.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c02_install_step_2_password.png" alt="Password provider">
 
#### Demo provider

The demo provider must **not** be selected in production, as the password **is** the username. It can be useful for a small test round for evaluating the product.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c02_install_step_2_demo.png" alt="Demo provider">

#### Ldap provider

If the users are stored in a ldap directory (Active Directory is supported too), the ldap provider must be configured.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c02_install_step_2_ldap.png" alt="Ldap provider">

It requires a user that can query the directory (the Manager DN and Manager Password). 

The query is composed by a base (Search base) and the filter (User search filter), where `{0}` is the placeholder for the username.

The configuration can be tested in the "Check ldap configuration" form.

#### Oauth provider

The application support the following external oauth providers: 

 - bitbucket 
 - gitlab.com
 - github
 - google
 - twitter
 
Additionally, self-hosted/others external gitlab instances can be configured in the Custom Oauth provider section.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c02_install_step_2_oauth.png" alt="Oauth provider">

#### Preconfigured oauth providers

Select the oauth provider of the first account and provide the api key and secret. The provided callback url should be the correct one that must be provided.

See the documentation for:

 - [bitbucket](https://confluence.atlassian.com/display/BITBUCKET/OAuth+on+Bitbucket)
 - [gitlab](http://doc.gitlab.com/ce/integration/oauth_provider.html). Registration page is https://gitlab.com/profile/applications
 - [github](https://developer.github.com/v3/oauth/). Registration page is https://github.com/settings/applications/new
 - [google](https://developers.google.com/identity/protocols/OAuth2WebServer): the "Google+ API" must be enabled
 - [twitter](https://dev.twitter.com/web/sign-in/implementing)
 
#### Custom oauth providers

If you have a self-hosted (or others) gitlab instance, you can configure in the "Custom oauth providers" section.
Select the type, enter the name, the base url, api key and api secret.

Please note that if you are using **self signed certificates** you _must_ include them in the default keystore of your java virtual machine. See the [keytool](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html#keytool_option_importcert) documentation and this [stackoverflow post](http://stackoverflow.com/a/11617655). Lavagna will **not** provide a way to ignore untrusted certificates. 



### Step 3 Insert administator

In the third step, the administrator must be defined. Enter the username and click **Next**.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c02_install_step_3.png" alt="Insert admin user">

### Step 4 Confirm

Check the validity of the configuration data. Click on **Activate**: the configuration will be saved and the browser will go to the root of the application. Enter the username (and password if required).

<img class="pure-img" src="{{relativeRootPath}}/images/en/c02_install_step_4.png" alt="Confirm">
