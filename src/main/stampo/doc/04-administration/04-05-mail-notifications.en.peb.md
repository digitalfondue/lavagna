## Mail notifications

For sending email notifications, a smtp server must be available and accessible.

Click on the toggle for displaying and enabling the smtp configuration form:

<img class="pure-img" src="{{relativeRootPath}}/images/en/admin-smtp.png" alt="SMTP configuration">

Consult https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-summary.html if there is a need to add additional properties.

Fields description, with a [mailgun](https://www.mailgun.com/) configuration as an example:

 - **Host**: the smtp host. Example: `smtp.mailgun.org`
 - **Port**: the smtp port. Example: 465
 - **Protocol**: smtp or smtps. Example: smtps
 - **Username**: smtp username. Example: example@example.com
 - **Password**: smtp password.
 - **From E-Mail**: the "from" value when lavagna send the email. Example: no-reply@dev.lavagna.io

You can test your configuration by clicking "send test email"
