package dsoluti.seo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;

/**
 * Simple HTTP Server that renders HTML pages using Selenium.
 * 
 */
public class App {

	private static final String regex = "^(?!.*?(\\.js|\\.css|\\.xml|\\.less|\\.png|\\.jpg|\\.jpeg|\\.gif|\\.pdf|\\.doc|\\.txt|\\.ico|\\.rss|\\.zip|\\.mp3|\\.rar|\\.exe|\\.wmv|\\.doc|\\.avi|\\.ppt|\\.mpg|\\.mpeg|\\.tif|\\.wav|\\.mov|\\.psd|\\.ai|\\.xls|\\.mp4|\\.m4a|\\.swf|\\.dat|\\.dmg|\\.iso|\\.flv|\\.m4v|\\.torrent|\\.ttf|\\.woff))(.*)";

	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
	private static final Logger START_REQUEST_LOGGER = LoggerFactory.getLogger("request-start-file");
	private static final Logger END_REQUEST_LOGGER = LoggerFactory.getLogger("request-end-file");

	public static void main(String[] args) {

		VertxOptions vertxOptions = new VertxOptions();
		vertxOptions.setMaxEventLoopExecuteTime(60000000000L); // 60 seconds

		HttpServer server = Vertx.vertx(vertxOptions).createHttpServer();

		server.requestHandler(request -> {
			
			START_REQUEST_LOGGER.info(request.getHeader("USER_AGENT") + request.host());
			 
			LOGGER.info(request.absoluteURI());
			// String base = "https://www.nextprot.org";
			String base = "https://bed-search.nextprot.org";

			String requestedUrl = base + request.path();

			LOGGER.info("Asking for " + requestedUrl + " and path is " + request.path());

			if (requestedUrl.matches(regex)) {

				LOGGER.info("Responding with selenium for " + request.absoluteURI());

				String url = request.absoluteURI().replaceAll("http://localhost:8082", base);
				String content = WebRemoteDriver.getContent(url);
				request.response().putHeader("content-type", "text/html").end(content);

			} else {

				LOGGER.info("Responding with redirect for " + request.absoluteURI());
				request.response().putHeader("location", requestedUrl).setStatusCode(302).end();
			}
			
			
			END_REQUEST_LOGGER.info(request.host());

		});

		Integer port = 8082;
		server.listen(port);
		System.out.println("Listening to port " + port);

	}

}
