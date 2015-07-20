## Get Started

This section is divided in multiple steps: first preparing the database, second configuring the connection parameters of lavagna and third the setup phase.

### Database

Lavagna support the following databases:

 - mysql version 5.1 or later
 - postgresql version 9.2 or later
 - embedded hsqldb version 2.3.1
 
#### MySql

If you are using mysql, for ensuring a good full text search experience you must set in the mysql configuration file:

```
[mysqld]
ft_min_word_len=3
``` 

The default 4 minimal word length can be problematic if you are using 3 letters acronyms.

Additionally, when creating the database, you must ensure that utf-8 is used everywhere:

```sql
CREATE DATABASE lavagna CHARACTER SET utf8 COLLATE utf8_bin;
```

#### Postgresql

For the full text search, the unaccent extension must be present. In general it's already included in the postgresql
installation. If it's not present, an error will be launched when running lavagna the first time.

### Configuration

Lavagna can run as a standalone application or inside a servlet 3.0+ container.

#### Self contained

As a standalone application, see the `bin/lavagna.sh`, `bin/lavagna.bat` or `bin/windows-service/lavagna.xml` scripts and configure them.
It will launch the app using a self contained jetty server.

#### Servlet container

You can deploy in any servlet 3.0 container. You will need to set the following
property to the JVM (see the scripts bin/lavagna.sh / bin/lavagna.bat):

 - datasource.driver=org.hsqldb.jdbcDriver | com.mysql.jdbc.Driver | org.postgresql.Driver
 - datasource.dialect=HSQLDB | MYSQL | PGSQL
 - datasource.url= for example: jdbc:hsqldb:mem:lavagna | jdbc:mysql://localhost:3306/lavagna | jdbc:postgresql://localhost:5432/lavagna
 - datasource.username=[username]
 - datasource.password=[pwd]
 - spring.profiles.active= dev | prod
 
 
### Setup

