## Configuration parameters

The configuration parameters section provides the list of the currently configured parameters in the application, e.g.: smtp configuration, authentication configuration, and the possibility to change a few.

TBD image 

The following parameters can be configured in this page:

* **EMAIL_NOTIFICATION_TIMESPAN**: In minutes. How often to check for new events and send email notifications. If not configured, the default value is 30
* **MAX_UPLOAD_FILE_SIZE**: Size in bytes. Limit the dimensions of the uploaded files. If not configured, no limit will be considered
* **TRELLO_API_KEY**: Trello api key to import boards, can be found at https://trello.com/app-key
* **USE_HTTPS**: true or false. If true the [Strict Transport Security](https://en.wikipedia.org/wiki/HTTP_Strict_Transport_Security) header will be sent and the application will be accessible only over a https connection.

To modify a parameter, enter the new value and click **Save**. To revert to the default value, click **Delete**.
