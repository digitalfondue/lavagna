## Database

Lavagna support the following databases:

 - mysql version 5.1 or later
 - postgresql version 9.2 or later
 - embedded hsqldb version 2.3.1
 
If it's not possible to use MySql or Postgresql and the full text search feature is not important, the hsqldb backend can be used with a file backend. See the hsqldb section below. 
 
### MySql

When using mysql, for ensuring a good full text search experience, it's advisable to set in the mysql configuration file:

```
[mysqld]
ft_min_word_len=3
``` 

The default 4 minimal word length can be problematic for searching 3 letters acronyms.

Additionally, when creating the database, it must ensured that utf-8 is used everywhere:

```sql
CREATE DATABASE lavagna CHARACTER SET utf8 COLLATE utf8_bin;
```

### Postgresql

When using Postgresql, ensure that the database/schema is in utf-8.

For the full text search support, the unaccent extension must be present. In general it's already included in the postgresql
installation. If it's not present, an error will be launched when running lavagna the first time.


### Hsqldb

It's the default development database. But it's possible to use it a persistent way: when configuring the jdbc url, the file backend can be used: `-Ddatasource.url=jdbc:hsqldb:file:lavagna `.