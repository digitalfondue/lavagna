## Database

Lavagna support the following databases:

 - mysql version 5.1 or later
 - postgresql version 9.2 or later
 - embedded hsqldb version 2.3.1
 
### MySql

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

### Postgresql

For the full text search, the unaccent extension must be present. In general it's already included in the postgresql
installation. If it's not present, an error will be launched when running lavagna the first time.


