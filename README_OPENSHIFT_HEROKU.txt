Openshift:

using this cartridge
http://cartreflect-claytondev.rhcloud.com/reflect?github=Worldline/openshift-cartridge-jetty-websocket

https://github.com/worldline/openshift-cartridge-jetty-websocket



Heroku:

Relevant urls:

 - https://devcenter.heroku.com/articles/deploy-a-java-web-application-that-launches-with-jetty-runner
 - https://devcenter.heroku.com/articles/connecting-to-relational-databases-on-heroku-with-java
 - https://devcenter.heroku.com/articles/heroku-labs-websockets

Procfile content:
--------------------
web:    java $JAVA_OPTS -Ddatasource.dialect=PGSQL -Ddatasource.driver=org.postgresql.Driver -Ddatasource.url=$DATABASE_URL -Dspring.profiles.active=prod -jar target/dependency/jetty-runner.jar --port $PORT target/*.war
--------------------

system.properties content:
--------------------
java.runtime.version=1.7
--------------------