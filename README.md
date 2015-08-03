Lavagna
======

[![Build Status](https://travis-ci.org/digitalfondue/lavagna.png?branch=master)](https://travis-ci.org/digitalfondue/lavagna)
[![Coverage Status](https://coveralls.io/repos/digitalfondue/lavagna/badge.svg?branch=master)](https://coveralls.io/r/digitalfondue/lavagna?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/io.lavagna/lavagna.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22lavagna%22)
[![Docker Status](https://img.shields.io/docker/pulls/digitalfondue/lavagna.svg)](https://registry.hub.docker.com/u/digitalfondue/lavagna/)

## About ##

Lavagna is a small and easy to use issue/project tracking software.

It requires Java 7 or better, MySQL (5.1 or better) or PostgreSQL. It can be deployed in a Java servlet container or as a self contained war.


## Install ##

Lavagna supports MySQL (at least 5.1) and PostgreSQL (tested on 9.1) for production use and HSQLDB for testing purposes.

It's distributed in 2 forms:

 - simple war for deploying in your preferred web container
 - self contained war with embedded jetty web server

### For testing purposes ###

If you want to test it locally, you can download the self contained war and run:

```
wget https://repo1.maven.org/maven2/io/lavagna/lavagna/1.0-M3/lavagna-1.0-M3-distribution.zip
unzip lavagna-1.0-M3-distribution.zip
./lavagna-1.0-M3/bin/lavagna.sh
```

Go to http://localhost:8080 and login with "user" (password "user").

See the README in the archive. A better documentation is arriving.

### Docker ###

Lavagna is also available as a Docker image so you can try it on the fly:

```
https://registry.hub.docker.com/u/digitalfondue/lavagna/
```

## Develop ##

### Java ###

Lavagna runs on Java 7, but requires Java 8 to build. (due to our documentation library)

### IDE Configuration ###

Lavagna uses project Lombok annotations, you will need to install the support in your IDE.

Use UTF-8 encoding and 120 characters as line width.


### Execute ###

Launch the Web Server:

```
mvn jetty:run
```

For launching Web Server + DB manager (HSQLDB only):

```
mvn jetty:run -DstartDBManager
```

for launching Web Server with the MySQL database (use the mysql profile):

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

For debugging:

```
mvnDebug jetty:run
```

For running the test cases:

```
mvn test
```

For running the test cases with MySQL or PostgreSQL:

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

When adding new file, remember to add the license header with:

```
mvn com.mycila:license-maven-plugin:format
```

### Documentation ###

The documentation is written using stampo (see https://github.org/digitalfondue/stampo).
It currently reside in src/main/stampo .

For building the doc:

```
mvn clean stampo:build
```

The output will be present in target/generated-docs

For testing the documentation run

```
mvn stampo:serve
```

And go to http://localhost:45001/

### Vagrant ###

In order to make it easier to tests on different databases we included 3 Vagrant VMs.
Make sure that you have installed Vagrant and VirtualBox before continuing.

#### Initialization ####

Fetch the submodules:

```
git submodule update --init
```

If you are under windows you need to ensure that the pgsql submodule is not in a broken state,
double check that the file puppet\modules\postgresql\files\validate_postgresql_connection.sh is using the
unix end of line (run dos2unix).

To run the tests with Vagrant boot the VMs with:

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

The application uses UTF-8 at every stage and on MySQL you will need to create a database with the collation set to utf8_bin:

```
CREATE DATABASE lavagna CHARACTER SET utf8 COLLATE utf8_bin;
```


#### Oracle support ####

(THIS SECTION SHOULD BE IGNORED)

First add the vbguest plugin:

> vagrant plugin install vagrant-vbguest

Note: if you have an error while installing the vagrant-vbguest plugin, see https://github.com/WinRb/vagrant-windows/issues/193 , install before the vagrant-login plugin with

> vagrant plugin install vagrant-login


Download Oracle Database 11g Express Edition for Linux x64 from ( http://www.oracle.com/technetwork/database/database-technologies/express-edition/downloads/index.html )

Place the file oracle-xe-11.2.0-1.0.x86_64.rpm.zip in the directory puppet/modules/oracle/files of this project.

Thanks to Hilverd Reker for his GitHub repo: https://github.com/hilverd/vagrant-ubuntu-oracle-xe .



### Code Coverage ###

Jacoco plugin is used.

```
mvn install site
```

-> open target/site/jacoco/index.html with your browser


## About Database migration ##

Can be disabled using the following system property: datasource.disable.migration=true


## Check for updated dependencies ##

Notes:

- HSQLDB at the moment will not be updated to version 2.3.2 due to a bug
  (default null+unique clause has changed)
- tomcat-jdbc will not be updated to version 8.0.9 due to a strange
  class loader interaction with log4j when launching with mvn jetty:run

```
mvn versions:display-dependency-updates
```
```
mvn versions:display-plugin-updates
```
