package dsoluti.seo;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
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

		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);

		// By default, any blocking handlers executed on the same context (e.g.
		// the same verticle instance) are ordered - this means the next one
		// won’t be executed until the previous one has completed. If you don’t
		// care about orderering and don’t mind your blocking handlers executing
		// in parallel you can set the blocking handler specifying ordered as
		// false using blockingHandler.
		// We don't care in this case, therfore order = false

		router.route().path("/favicon.ico").handler(routingContext -> {
			routingContext.response().setStatusCode(404).end();
		});

		router.route().handler(new GelfLoggerHandler());
		router.route().handler(routingContext -> {

			HttpServerRequest request = routingContext.request();
			startRequest(request.absoluteURI(), httpResponse -> {
				request.response().putHeader("content-type", "text/html").setStatusCode(httpResponse.status).end(httpResponse.content);
			});
		});

		Integer port = 8082;
		server.requestHandler(router::accept).listen(port);

		System.out.println("Listening on port " + port);

	}

	private static class HTTPResponse {
		String content;
		Integer status;

		public HTTPResponse(String content, Integer status) {
			this.content = content;
			this.status = status;
		}

		public HTTPResponse(String content) {
			this(content, 200);
		}
	}

	private void startRequest(String absoluteUri, Handler<HTTPResponse> done) {

		String absoluteUrl = absoluteUri.replace("?_escaped_fragment_=", "");

		if (absoluteUri.contains("favicon.ico")) {
			done.handle(new HTTPResponse("Favicon not found", 404));
		} else {

			final Matcher matcher = patternToGetUrl.matcher(absoluteUrl);

			String requestedUrl = null;
			if (matcher.find()) {
				requestedUrl = matcher.group(1);
			}

			final Matcher domainMatcher = patternToGetDomain.matcher(requestedUrl);
			String requestedDomain = null;
			if (domainMatcher.find()) {
				requestedDomain = domainMatcher.group(1);
			}

			if (requestedUrl.length() < 10) {
				done.handle(new HTTPResponse("Request url is not valid: " + requestedUrl, 400));
			} else {

				vertx.eventBus().send("render", requestedUrl, response -> {
					if (response.succeeded()) {
						done.handle(new HTTPResponse(response.result().body().toString()));
					}
				});
			}

		}
	}

	public void stop() {
		System.out.println("BOUM ******************************************************");
	}

}
