  _
 | |
 | |     __ ___   ____ _  __ _ _ __   __ _
 | |    / _` \ \ / / _` |/ _` | '_ \ / _` |
 | |___| (_| |\ V / (_| | (_| | | | | (_| |
 |______\__,_| \_/ \__,_|\__, |_| |_|\__,_|
                          __/ |
                         |___/

 Version: ${project.version}

 http://lavagna.io
===========================================

This archive contain:

 - /bin/* : shell script for launching lavagna. By default it will launch
            lavagna in dev mode. You will need to customize the scripts.

 - /lavagna/*.war : a simple war that can be deployed in your servlet
                    container (tomcat, ...) or an executable war
                    (which is launched by the scripts)

 - /LICENSE.txt: license file (GPL3)
 - /NOTICE.txt: third party licenses file


Running the application
-----------------------

For testing purpose you can launch bin/lavagna.sh, bin/lavagna.bat or bin/windows-service/lavagna.xml and go
to http://localhost:8080 (username: user password: user).

If you want to install it or configure it, you have two options:

- Self contained
- Deploy in a servlet container


Database configuration
----------------------

Lavagna require a utf8 environment.
Check that the database has a utf8 collation.

For ensuring a correct db creation with MySQL create it with:

CREATE DATABASE lavagna CHARACTER SET utf8 COLLATE utf8_bin;


Self contained:
---------------

See the bin/lavagna.sh, bin/lavagna.bat or bin/windows-service/lavagna.xml scripts and configure them.
It will launch the app using a self contained jetty server.


Servlet container:
------------------

You can deploy in any servlet 3.0 container. You will need to set the following
property to the JVM (see the scripts bin/lavagna.sh / bin/lavagna.bat):

 - datasource.dialect=HSQLDB | MYSQL | PGSQL
 - datasource.url= for example: jdbc:hsqldb:mem:lavagna | jdbc:mysql://localhost:3306/lavagna?useUnicode=true&characterEncoding=utf-8 | jdbc:postgresql://localhost:5432/lavagna
 - datasource.username=[username]
 - datasource.password=[pwd]
 - spring.profiles.active= dev | prod

Or

You can define them in an external file (like the bundled sample-conf.properties) and define the following property:

 - lavagna.config.location=file:/your/file/location.properties
