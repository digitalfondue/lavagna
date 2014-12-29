README
======

## ABOUT ##

Lavagna is a small and easy to use agile issue/project tracking software.

It require : java 7 or better, mysql (5.1 or better) or postgresql. It can be deployed in a java servlet container.


## INSTALL ##

Lavagna support mysql (at least 5.1) / pgsql for production use and hsqldb for testing purpose.

It's distributed in 2 forms:

 - simple war for deploying in your preferred web container
 - self contained war with embedded jetty web server
 
### For trial purpose ###

If you want to test it locally, you can download the self contained war and run (java 7 or greater required):

```
java -Ddatasource.driver=org.hsqldb.jdbcDriver -Ddatasource.dialect=HSQLDB -Ddatasource.url=jdbc:hsqldb:mem:lavagna -Ddatasource.username=sa -Ddatasource.password= -Dspring.profile.active=dev -jar lavagna-jetty-console.war --headless
```

Go to http://localhost:8080 and login with "user" (password "user").

### Setup ###

Lavagna require the following property to be set on the jvm:

 - datasource.driver=org.hsqldb.jdbcDriver | com.mysql.jdbc.Driver | org.postgresql.Driver
 - datasource.dialect=HSQLDB | MYSQL | PGSQL
 - datasource.url= for example: jdbc:hsqldb:mem:lavagna | jdbc:mysql://localhost:3306/lavagna | jdbc:postgresql://localhost:5432/lavagna
 - datasource.username=<username>
 - datasource.password=<pwd> 
 - spring.profile.active= dev | prod
 
The db user must be able to create tables and others db objects.

Once the application has been started/deployed, go to 

http(s)://<your deploy>(:port)/setup/

There you can:

1. configure the application
2. import a lavagna export

### Configuration steps ###

1. define the base url
2. define the initial login configuration (demo, ldap, oauth, mozilla persona)
3. define the admin user
4. confirm


## DEVELOP ##

### IDE Configuration ###

This project use project lombok annotations, you will need to install the support in your IDE.

Use UTF-8 encoding.


### Execute ###

launch web server:

```
mvn jetty:run
```

for launching web server + db manager (hsqldb only)

```
mvn jetty:run -DstartDBManager
```

for launching web server with the mysql database (use mysql profile):

```
mvn jetty:run -Pdev-mysql
```
```
mvn jetty:run -Pdev-pgsql
```
- go to http://localhost:8080
  if you have a 403 error, you must configure the application,
  go to http://localhost:8080/setup, select demo + insert user "user".

- enter 
	username: user
	password: user 

For debugging

```
mvnDebug jetty:run
```

For running the test cases

```
mvn test
```

For running the test cases with mysql or pgsql

```
mvn test -Ddatasource.dialect=MYSQL
```
```
mvn test -Ddatasource.dialect=PGSQL
```

For running with jetty-runner:

```
mvn clean install
java -Ddatasource.dialect=HSQLDB -Ddatasource.driver=org.hsqldb.jdbcDriver -Ddatasource.url=jdbc:hsqldb:mem:lavagna -Ddatasource.username=sa -Ddatasource.password= -Dspring.profiles.active=dev -jar target/dependency/jetty-runner.jar --port 8080 target/*.war
```

### VAGRANT ###

Make sure that you have installed Vagrant and VirtualBox.

#### Initialization ####

Fetch the submodules before:

```
git submodule update --init
```

If you are under windows you need to ensure that the pgsql submodule is not in a broken state, 
ensure that the file puppet\modules\postgresql\files\validate_postgresql_connection.sh is using the
unix end of line (run dos2unix).

To run the tests with Vagrant boot the VMs with

```
vagrant up [optionally use pgsql / mysql to boot only one VM]
```

Once that the VM is up and running run the tests:

```
mvn test -Ddatasource.dialect=PGSQL / MYSQL
```


#### Connecting manually: ####
 
PGSQL: localhost:5432/lavagna as postgres / password
 
MySQL: localhost:3306/lavagna as root
 
Oracle: localhost:1521/XE as system / manager

## Notes about databases ##

The application use UTF-8 at every stage, on mysql you will need to create a database with the collation set to utf8_bin :

CREATE DATABASE lavagna CHARACTER SET utf8 COLLATE utf8_bin;




#### Oracle support ####

(THIS SECTION SHOULD BE IGNORED)

First add the vbguest plugin:

> vagrant plugin install vagrant-vbguest

Note: if you have an error while installing the vagrant-vbguest plugin, see https://github.com/WinRb/vagrant-windows/issues/193 , install before the vagrant-login plugin with

> vagrant plugin install vagrant-login


Download Oracle Database 11g Express Edition for Linux x64 from ( http://www.oracle.com/technetwork/database/database-technologies/express-edition/downloads/index.html )

Place the file oracle-xe-11.2.0-1.0.x86_64.rpm.zip in the directory puppet/modules/oracle/files of this project. 

Thanks to Hilverd Reker for his GitHub repo: https://github.com/hilverd/vagrant-ubuntu-oracle-xe .



### CODE COVERAGE ###

Jacoco plugin is used.

```
mvn install site
```

-> open target/site/jacoco/index.html with your browser

## DATABASE MIGRATION ##

Can be disabled using the following system property: datasource.disable.migration=true


## CHECK FOR UPDATED DEPENDENCIES ##

Note: 

- hsqldb atm will not be updated to version 2.3.2 due to a bug 
  (default null+unique clause has changed)
- tomcat-jdbc will not be updated to version 8.0.9 due to a strange 
  class loader interaction with log4j when launching with mvn jetty:run

```
mvn versions:display-dependency-updates
```
```
mvn versions:display-plugin-updates 
```
