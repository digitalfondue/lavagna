Lavagna
======

[![Join the chat at https://gitter.im/digitalfondue/lavagna](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/digitalfondue/lavagna?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](https://travis-ci.org/digitalfondue/lavagna.svg?branch=master)](https://travis-ci.org/digitalfondue/lavagna)
[![Coverage Status](https://coveralls.io/repos/digitalfondue/lavagna/badge.svg?branch=master)](https://coveralls.io/r/digitalfondue/lavagna?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/io.lavagna/lavagna.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22lavagna%22)
[![Github All Releases](https://img.shields.io/github/downloads/digitalfondue/lavagna/total.svg)](https://github.com/digitalfondue/lavagna/releases)
[![Docker Status](https://img.shields.io/docker/pulls/digitalfondue/lavagna.svg)](https://registry.hub.docker.com/u/digitalfondue/lavagna/)
[![Docker Layers](https://images.microbadger.com/badges/image/digitalfondue/lavagna.svg)](https://microbadger.com/images/digitalfondue/lavagna)

# Latest stable release is 1.0.7.4 (2017-07-03) #
# Latest pre-release is 1.1-M6 (2017-07-03) #

## About ##

[Lavagna](http://lavagna.io) is a small and easy to use issue/project tracking software.

It requires Java 7 or better and optionally a database: MySQL, MariaDB or PostgreSQL. It can be deployed in a Java servlet container or as a self contained war.

See:

 - [roadmap](https://github.com/digitalfondue/lavagna/blob/master/ROADMAP.md)
 - [changelog](https://github.com/digitalfondue/lavagna/blob/master/CHANGELOG.md)
 - [download](http://lavagna.io/download/)


## Install ##

Lavagna supports MySQL (at least 5.1), MariaDB (tested on 10.1), PostgreSQL (tested on 9.1) and HSQLDB (for small deploy).

It's distributed in 2 forms:

 - simple war for deploying in your preferred web container
 - self contained war with embedded jetty web server
 
See the documentation at http://help.lavagna.io

### For testing purposes ###

If you want to test it locally, you can download the self contained war and run:

```
wget https://repo1.maven.org/maven2/io/lavagna/lavagna/1.0.7.4/lavagna-1.0.7.4-distribution.zip
unzip lavagna-1.0.7.4-distribution.zip
./lavagna-1.0.7.4/bin/lavagna.sh
```

or the milestone release:

```
wget https://repo1.maven.org/maven2/io/lavagna/lavagna/1.1-M6/lavagna-1.1-M6-distribution.zip
unzip lavagna-1.1-M6-distribution.zip
./lavagna-1.1-M6/bin/lavagna.sh
```

Go to http://localhost:8080 and login with "user" (password "user").

See the README in the archive and the documentation at http://help.lavagna.io if you want to customize the scripts and set lavagna in production mode.

### Docker ###

Lavagna is also available as a Docker image so you can try it on the fly:

```
https://registry.hub.docker.com/u/digitalfondue/lavagna/
```

### On openshift ###

See the guide at http://lavagna.io/help/openshift/

## Develop ##

### Java and Kotlin ###

Lavagna runs on a Java 7 jvm, but requires Java 8 to build. (due to our documentation library).
Some parts of Lavagna are made with Kotlin.

### IDE Configuration ###

Use UTF-8 encoding and 120 characters as line width.
You will need a Java _and_ Kotlin aware IDE. (Currently tested with intellij and eclipse).

For eclipse: you will need to install the kotlin plugin and add the "Kotlin nature" to the project:
Right click on the project -> "Configure Kotlin" -> "Add Kotlin nature"

### Javascript

Install npm and run the following script to assure your code follows our guidelines

First ensure that all the dependencies are ok with `npm install`.

Then, for checking:

```
npm run-script lint
```

Fix any error or warning before opening a pull request

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
java -Ddatasource.dialect=HSQLDB -Ddatasource.url=jdbc:hsqldb:mem:lavagna -Ddatasource.username=sa -Ddatasource.password= -Dspring.profiles.active=dev -jar target/dependency/jetty-runner.jar --port 8080 target/*.war
```

When adding new file, remember to add the license header with:

```
mvn com.mycila:license-maven-plugin:format
```

### Angular perfs ###

Use the following stats for keeping an eye on the performances:

 - https://github.com/kentcdodds/ng-stats has a bookmarklet
 - https://github.com/mrdoob/stats.js/ has a bookrmarklet

### Documentation ###

The documentation is written using stampo (see https://github.com/digitalfondue/stampo).
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

## Notes about databases ##

The application uses UTF-8 at every stage and on MySQL you will need to create a database with the collation set to utf8_bin:

```
CREATE DATABASE lavagna CHARACTER SET utf8 COLLATE utf8_bin;
```


### Code Coverage ###

Jacoco plugin is used.

```
mvn clean test jacoco:report
```

-> open target/site/jacoco/index.html with your browser


## About Database migration ##

Can be disabled using the following system property: datasource.disable.migration=true


## Check for updated dependencies ##

```
mvn versions:display-dependency-updates
```
```
mvn versions:display-plugin-updates
```
