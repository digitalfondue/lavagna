A second war named lavagna-jetty-console.war will be built. Inside there is an embedded jetty.

You must provide the following properties:

- datasource.driver=org.hsqldb.jdbcDriver | com.mysql.jdbc.Driver | org.postgresql.Driver
- datasource.dialect=HSQLDB | MYSQL | PGSQL
- datasource.url= for example: jdbc:hsqldb:mem:lavagna | jdbc:mysql://localhost:3306/lavagna | jdbc:postgresql://localhost:5432/lavagna
- datasource.username=<username>
- datasource.password=<pwd> 
- spring.profile.active= dev | prod

For example:

>java -Ddatasource.driver=org.hsqldb.jdbcDriver -Ddatasource.dialect=HSQLDB -Ddatasource.url=jdbc:hsqldb:mem:lavagna -Ddatasource.username=sa -Ddatasource.password= -Dspring.profile.active=dev -jar lavagna-jetty-console.war --headless


You can set port and others options too, see: http://simplericity.com/2009/11/10/1257880778509.html