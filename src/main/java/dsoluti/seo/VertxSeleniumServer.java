package dsoluti.seo;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;

/**
 * Simple HTTP Server that renders HTML pages using Selenium.
 * 
 */
public class VertxSeleniumServer extends AbstractVerticle {
	
	static final String BASE_URL = App.prop.get("base.url");
	static final String regexToGetUrl = "http.\\/\\/.*?\\/(.*)";
	static final String regexToGetDomain = "(http.:\\/\\/.*?\\/)";
	static final Pattern patternToGetUrl = Pattern.compile(regexToGetUrl);
	static final Pattern patternToGetDomain = Pattern.compile(regexToGetDomain);

    private static final Logger LOGGER = Logger.getLogger(VertxSeleniumServer.class.getName());

    @Override
    public void start() throws Exception {
    	
		VertxOptions vertxOptions = new VertxOptions();
		vertxOptions.setMaxEventLoopExecuteTime(60000000000L); // 60 seconds

		Vertx vertx = Vertx.vertx(vertxOptions);
		
		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);

		//By default, any blocking handlers executed on the same context (e.g. the same verticle instance) are ordered - this means the next one won’t be executed until the previous one has completed. If you don’t care about orderering and don’t mind your blocking handlers executing in parallel you can set the blocking handler specifying ordered as false using blockingHandler.
		//We don't care in this case, therfore order = false
		boolean ordered = false;
		
		router.route().path("/favicon.ico").blockingHandler(routingContext -> {
			routingContext.response().setStatusCode(404).end();
		});
		
		router.route().handler(new GelfLoggerHandler());
		router.route().blockingHandler(routingContext -> {
			
			HttpServerRequest request = routingContext.request();
			
			String absoluteUrl = request.absoluteURI().replace("?_escaped_fragment_=", "");
			
			if(request.path().contains("favicon.ico")){
				request.response().setStatusCode(404).end();
			}else {

				final Matcher matcher = patternToGetUrl.matcher(absoluteUrl);
				
				String requestedUrl = null;
				if (matcher.find()) { requestedUrl = matcher.group(1); }

				final Matcher domainMatcher = patternToGetDomain.matcher(requestedUrl);
				String requestedDomain = null;
				if (domainMatcher.find()) { requestedDomain = domainMatcher.group(1); }
				
				if(requestedUrl.length()<10){
					request.response().setStatusCode(400).end("Request url is not valid: " + requestedUrl);
				}else {

					//TODO requestedDomain should be used here...
					String content = WebRemoteDriver.getContent(BASE_URL, requestedUrl);
					request.response().putHeader("content-type", "text/html").end(content);

				}


			}
			
			// Now call the next handler
			//routingContext.next();

		}, ordered);
		


		Integer port = 8082;
		server.requestHandler(router::accept).listen(port);
		
		System.out.println("Listening on port " + port);

	}

}
