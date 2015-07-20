## Configuration

Lavagna can run as a standalone application or inside a servlet 3.0+ container.

### Self contained

As a standalone application, see the `bin/lavagna.sh`, `bin/lavagna.bat` or `bin/windows-service/lavagna.xml` scripts and configure them. It will launch the app using a self contained jetty server.

As a default, lavagna launch in dev mode, so check in the scripts that the spring.profiles.active property is set to prod.

### Servlet container

You can deploy in any servlet 3.0 container. You will need to set the following
property to the JVM (see the scripts bin/lavagna.sh / bin/lavagna.bat):

 - datasource.driver=org.hsqldb.jdbcDriver | com.mysql.jdbc.Driver | org.postgresql.Driver
 - datasource.dialect=HSQLDB | MYSQL | PGSQL
 - datasource.url= for example: jdbc:hsqldb:mem:lavagna | jdbc:mysql://localhost:3306/lavagna | jdbc:postgresql://localhost:5432/lavagna
 - datasource.username=[username]
 - datasource.password=[pwd]
 - spring.profiles.active= dev | prod