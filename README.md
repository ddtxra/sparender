# Sparender
Sparender is an open-source and free to use tool, that will help your website beeing indexed properly by search engine bots (such as: Google, Bing, Yahoo, Yandex, …)

# Made for SPAs (Singe Page Applications)
Sparender works well with SPAs (Single Page Application) using any of the latest javascript frameworks such as: AngularJS, ReactJS, Web Components (including Google Polymer), VueJS, EmberJS, etc…


Set up selenium first
```
docker run --name selenium5 -d -p 4444:4444 -p 5900:5900 -e JAVA_OPTS=-Xmx2048m --add-host=www.google-analytics.com:127.0.0.1 -v /dev/shm:/dev/shm selenium/standalone-chrome:3.4.0-dysprosium
```

Access your browser at localhost:8082/YOUR_WEBSITE, e.g.:

http://localhost:8082/https://karibou.ch/products/category/fruits-legumes


Optionally setup a graylog instance to have insights of the top robots
```
docker run --name some-mongo -d mongo:2
docker run --name some-elasticsearch -d elasticsearch:2 elasticsearch -Des.cluster.name="graylog"
docker run --link some-mongo:mongo --link some-elasticsearch:elasticsearch -p 9000:9000 -e GRAYLOG_WEB_ENDPOINT_URI="http://127.0.0.1:9000/api" -d graylog2/server
```


You can easily configure apache as follows:
```

```

Tuning options
```

```

