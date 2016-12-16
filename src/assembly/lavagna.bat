@echo off
rem lavagna launcher
rem
rem
rem As a default, it will launch the application in dev mode for testing purpose. You will need to customize this script.
rem
rem Database choice:
rem ----------------
rem  - HSQLDB (for in memory test or for low volumes using his file store)
rem    You must set:
rem
rem    -Ddatasource.dialect=HSQLDB
rem
rem  - MySQL
rem    You must set:
rem
rem    -Ddatasource.dialect=MYSQL
rem
rem  - PostgreSQL
rem    You must set:
rem
rem    -Ddatasource.dialect=PGSQL
rem
rem Database configuration:
rem -----------------------
rem You will need to set the following 3 variables:
rem
rem -Ddatasource.url=
rem  Check the driver documentation, most likely the connection string will have the following form:
rem  - HSQDLB: -Ddatasource.url=jdbc:hsqldb:mem:lavagna
rem  - MYSQL: -Ddatasource.url=jdbc:mysql://localhost:3306/lavagna?useUnicode=true&characterEncoding=utf-8
rem  - PGSQL: -Ddatasource.url=jdbc:postgresql://localhost:5432/lavagna
rem
rem Self explanatory:
rem -Ddatasource.username=sa
rem -Ddatasource.password=
rem
rem Select the profile:
rem -------------------
rem -Dspring.profiles.active=prod
rem or
rem -Dspring.profiles.active=dev
rem
rem You will likely want to use prod (as in production) mode.
rem
rem
rem Hostname/contextPath/port/temporary directory:
rem ----------------------------------
rem You can set port and others options too:
rem
rem Options:
rem --port n            - Create an HTTP listener on port n (default 8080)
rem --bindAddress addr  - Accept connections only on address addr (default: accept on any address)
rem --contextPath /path - Set context path (default: /)
rem --tmpDir /path      - Temporary directory, default is /tmp
rem --cookiePrefix NAME - Set a prefix to the cookie name, so you can have multiple lavagna instances on the same hostname
rem
rem example: java -jar lavagna-jetty-console.war --port 8081 --bindAddress 127.0.0.1
rem
rem example: java -Dlavagna.config.location=file:/C:\... -jar lavagna-jetty-console.war --port 8081 --bindAddress 127.0.0.1

java ^
	-Ddatasource.dialect=HSQLDB ^
	-Ddatasource.url=jdbc:hsqldb:mem:lavagna ^
	-Ddatasource.username=sa ^
	-Ddatasource.password= ^
	-Dspring.profiles.active=dev ^
	-jar %~dp0/../lavagna/lavagna-jetty-console.war
