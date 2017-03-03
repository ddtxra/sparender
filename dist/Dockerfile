FROM anapsix/alpine-java
COPY app.jar /home/app.jar
CMD ["java","-Dselenium.url=http://selenium:4444/wd/hub", "-Dgraylog.host=localhost", "-Dgraylog.port=5556", "-jar","/home/app.jar"]
