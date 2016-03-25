A second war named lavagna-jetty-console.war will be built. Inside there is an embedded jetty.

You must provide the following properties:

- datasource.dialect=HSQLDB | MYSQL | PGSQL
- datasource.url= for example: jdbc:hsqldb:mem:lavagna | jdbc:mysql://localhost:3306/lavagna | jdbc:postgresql://localhost:5432/lavagna
- datasource.username=<username>
- datasource.password=<pwd> 
- spring.profile.active= dev | prod

For example:

>java -Ddatasource.dialect=HSQLDB -Ddatasource.url=jdbc:hsqldb:mem:lavagna -Ddatasource.username=sa -Ddatasource.password= -Dspring.profile.active=dev -jar lavagna-jetty-console.war


You can set port and others options too:

Options:
 --port n            - Create an HTTP listener on port n (default 8080)
 --bindAddress addr  - Accept connections only on address addr (default: accept on any address)
 --contextPath /path - Set context path (default: /)
 --help              - Print this help message
 --tmpDir /path      - Temporary directory, default is /tmp