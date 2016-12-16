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
#    -Ddatasource.dialect=HSQLDB
#
#  - MySQL
#    You must set:
#
#    -Ddatasource.dialect=MYSQL
#
#  - PostgreSQL
#    You must set:
#
#    -Ddatasource.dialect=PGSQL
#
# Database configuration:
# -----------------------
# You will need to set the following 3 variables:
#
# -Ddatasource.url=
#  Check the driver documentation, most likely the connection string will have the following form:
#  - HSQDLB: -Ddatasource.url=jdbc:hsqldb:mem:lavagna
#  - MYSQL: -Ddatasource.url=jdbc:mysql://localhost:3306/lavagna?useUnicode=true&characterEncoding=utf-8
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
#
#
# Hostname/contextPath/port/temporary directory:
# ----------------------------------
# You can set port and others options too:

# Options:
# --port n            - Create an HTTP listener on port n (default 8080)
# --bindAddress addr  - Accept connections only on address addr (default: accept on any address)
# --contextPath /path - Set context path (default: /)
# --tmpDir /path      - Temporary directory, default is /tmp
# --cookiePrefix NAME - Set a prefix to the cookie name, so you can have multiple lavagna instances on the same hostname
#
# example: java -jar lavagna-jetty-console.war --port 8081 --bindAddress 127.0.0.1
#
# example: java -Dlavagna.config.location=file:/opt.. -jar lavagna-jetty-console.war --port 8081 --bindAddress 127.0.0.1

BASEDIR=$(dirname $0)

java \
	-Ddatasource.dialect=HSQLDB \
	-Ddatasource.url=jdbc:hsqldb:mem:lavagna \
	-Ddatasource.username=sa \
	-Ddatasource.password= \
	-Dspring.profiles.active=dev \
	-jar $BASEDIR/../lavagna/lavagna-jetty-console.war
