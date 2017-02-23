package dsoluti.seo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	//private static final String regex = "^(?!.*?(\\.js|\\.css|\\.xml|\\.less|\\.png|\\.jpg|\\.jpeg|\\.gif|\\.pdf|\\.doc|\\.txt|\\.ico|\\.rss|\\.zip|\\.mp3|\\.rar|\\.exe|\\.wmv|\\.doc|\\.avi|\\.ppt|\\.mpg|\\.mpeg|\\.tif|\\.wav|\\.mov|\\.psd|\\.ai|\\.xls|\\.mp4|\\.m4a|\\.swf|\\.dat|\\.dmg|\\.iso|\\.flv|\\.m4v|\\.torrent|\\.ttf|\\.woff))(.*)";

	static final String regexToGetUrl = "http.\\/\\/.*?\\/(.*)";
	static final String regexToGetDomain = "(http.:\\/\\/.*?\\/)";
	static final Pattern patternToGetUrl = Pattern.compile(regexToGetUrl);
	static final Pattern patternToGetDomain = Pattern.compile(regexToGetDomain);
	

	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
	private static final Logger START_REQUEST_LOGGER = LoggerFactory.getLogger("request-start-file");
	private static final Logger END_REQUEST_LOGGER = LoggerFactory.getLogger("request-end-file");

	public static void main(String[] args) {

		VertxOptions vertxOptions = new VertxOptions();
		vertxOptions.setMaxEventLoopExecuteTime(60000000000L); // 60 seconds

		HttpServer server = Vertx.vertx(vertxOptions).createHttpServer();

		server.requestHandler(request -> {

			START_REQUEST_LOGGER.info(request.getHeader("USER_AGENT") + request.host());
			
			///http://localhost:8082/https://bed-search.nextprot.org/proteins/search?query=MSH6
				
			LOGGER.info(request.absoluteURI()); //         http://localhost:8082/https://bed-search.nextprot.org/proteins/search?query=MSH6
			LOGGER.info(request.path()); //                /https://bed-search.nextprot.org/proteins/search
			
			String absoluteUrl = request.absoluteURI().replace("?_escaped_fragment_=", "");
			LOGGER.info(absoluteUrl);
			
			if(request.path().contains("favicon.ico")){
				request.response().setStatusCode(404).end();
			}else {

				final Matcher matcher = patternToGetUrl.matcher(absoluteUrl);
				
				String requestedUrl = null;
				if (matcher.find()) { requestedUrl = matcher.group(1); }

				final Matcher domainMatcher = patternToGetDomain.matcher(requestedUrl);
				String requestedDomain = null;
				if (domainMatcher.find()) { requestedDomain = domainMatcher.group(1); }

				System.err.println("requestedDomain" + requestedDomain);

				LOGGER.info("Asking for " + requestedUrl + " and path is " + request.path());

				System.err.println("REQUESTED URL" + requestedUrl);

				String content = WebRemoteDriver.getContent("https://bed-search.nextprot.org/", requestedUrl);
				request.response().putHeader("content-type", "text/html").end(content);

				END_REQUEST_LOGGER.info(request.host());

			}


		});

		Integer port = 8082;
		server.listen(port);
		System.out.println("Listening to port " + port);

	}

}
