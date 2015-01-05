#!/bin/bash
#
# lavagna launcher
#
#
# As a default, it will launch the application in dev mode for testing purpose. You will need to customize this script.
#
# Database choice:
# ----------------
#  - HSQLDB (for in memory test or for low volumes using his file store)
#    You must set:
#
#    -Ddatasource.driver=org.hsqldb.jdbcDriver
#    -Ddatasource.dialect=HSQLDB
# 
#  - MySQL
#    You must set:
#
#    -Ddatasource.driver=com.mysql.jdbc.Driver
#    -Ddatasource.dialect=MYSQL
#
#  - PostgreSQL
#    You must set:
#
#    -Ddatasource.driver=org.postgresql.Driver
#    -Ddatasource.dialect=PGSQL
#
# Database configuration:
# -----------------------
# You will need to set the following 3 variables:
# 
# -Ddatasource.url=
#  Check the driver documentation, most likely the connection string will have the following form:
#  - HSQDLB: -Ddatasource.url=jdbc:hsqldb:mem:lavagna
#  - MYSQL: -Ddatasource.url=jdbc:mysql://localhost:3306/lavagna
#  - PGSQL: -Ddatasource.url=jdbc:postgresql://localhost:5432/lavagna
#
# Self explanatory:
# -Ddatasource.username=sa
# -Ddatasource.password=
#
# Select the profile:
# -------------------
# -Dspring.profiles.active=prod
# or
# -Dspring.profiles.active=dev
#
# You will likely want to use prod (as in production) mode.

BASEDIR=$(dirname $0)

java \
	-Ddatasource.driver=org.hsqldb.jdbcDriver \
	-Ddatasource.dialect=HSQLDB \
	-Ddatasource.url=jdbc:hsqldb:mem:lavagna \
	-Ddatasource.username=sa \
	-Ddatasource.password= \
	-Dspring.profiles.active=dev \
	-jar $BASEDIR/../lavagna/lavagna-jetty-console.war --headless