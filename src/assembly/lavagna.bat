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
rem    -Ddatasource.driver=org.hsqldb.jdbcDriver
rem    -Ddatasource.dialect=HSQLDB
rem 
rem  - MySQL
rem    You must set:
rem
rem    -Ddatasource.driver=com.mysql.jdbc.Driver
rem    -Ddatasource.dialect=MYSQL
rem
rem  - PostgreSQL
rem    You must set:
rem
rem    -Ddatasource.driver=org.postgresql.Driver
rem    -Ddatasource.dialect=PGSQL
rem
rem Database configuration:
rem -----------------------
rem You will need to set the following 3 variables:
rem 
rem -Ddatasource.url=
rem  Check the driver documentation, most likely the connection string will have the following form:
rem  - HSQDLB: -Ddatasource.url=jdbc:hsqldb:mem:lavagna
rem  - MYSQL: -Ddatasource.url=jdbc:mysql://localhost:3306/lavagna
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


java \
	-Ddatasource.driver=org.hsqldb.jdbcDriver \
	-Ddatasource.dialect=HSQLDB \
	-Ddatasource.url=jdbc:hsqldb:mem:lavagna \
	-Ddatasource.username=sa \
	-Ddatasource.password= \
	-Dspring.profiles.active=dev \
	-jar ../lavagna/lavagna-jetty-console.war --headless