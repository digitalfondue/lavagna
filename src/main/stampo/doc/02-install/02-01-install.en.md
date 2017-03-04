## Database

Lavagna support the following databases:

 - MySQL version 5.1 or later
 - MariaDB version 10.0 or later
 - PostgreSQL version 9.2 or later
 - embedded HSQLDB version 2.3.1
 
If it's not possible to use MySql or Postgresql and the full text search feature is not important, the HSQLDB backend can be used with a file backend. See the HSQLDB section below. 
 
### MySql

When using MySql, for ensuring a good full text search experience, it's advisable to set in the MySql configuration file:

```
[mysqld]
ft_min_word_len=3
ft_stopword_file = ""
``` 

The default minimal word length (4) and the stopwords can be problematic for searching acronyms or short words.

Additionally, when creating the database, it must ensured that utf-8 is used everywhere:

```sql
CREATE DATABASE lavagna CHARACTER SET utf8 COLLATE utf8_bin;
```

### MariaDB

Unit tests are run also on MariaDB and as a general rule everything that must be done for MySql must be done also for MariaDB.

### PostgreSQL

When using PostgreSQL, ensure that the database/schema is in utf-8.

For the full text search support, the unaccent extension must be present. In general it's already included in the PostgreSQL
installation. If it's not present, an error will be launched when running lavagna the first time.


### HSQLDB

It's the default development database. But it's possible to use it a persistent way: when configuring the jdbc url, the file backend can be used: `-Ddatasource.url=jdbc:hsqldb:file:lavagna `.
