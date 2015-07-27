## Import/Export data

For creating a backup of lavagna, exporting the dabase (with [mysqdump](https://dev.mysql.com/doc/refman/5.1/en/mysqldump.html) or [pg_dump](http://www.postgresql.org/docs/9.4/static/app-pgdump.html)) is the recommended method.

The import/export data functionality provided by lavagna has some serious drawback, but it can be used as a backup method and/or transport data to a different database (e.g.: from mysql to pgsql).

The drawbacks are: 

 - if a project with the same short name already exists in the target system, the whole project will not be imported  
 - it's possible that some part of the history cannot be rebuilt
 
### Exporting

For exporting click the export button. The process will take some time, especially if there are uploaded files.

<img class="pure-img" src="{{relativeRootPath}}/images/en/c03_admin_import_export_export.png" alt="Export">

The resulting file is a zip archive named lavagna-YEAR-MONTH-DAY.zip.

### Importing

First select the archive to import.

When importing the file, by default, the current configuration is preserved. For using the configuration from the zip archive, check the checkbox. 

Click the "Import" button for launching the import procedure. The import will take some time and can use a sizeable amount of memory on the database, as the operation is done in a single transaction. 

<img class="pure-img" src="{{relativeRootPath}}/images/en/c03_admin_import_export_import.png" alt="Import">

