mvn clean package
cp target/vertx--selenium-server-1.0.0-fat.jar dist/app.jar
docker-compose build vertx-selenium-server


