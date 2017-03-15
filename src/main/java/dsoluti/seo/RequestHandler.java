package dsoluti.seo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class RequestHandler extends AbstractHandler implements Handler {

	private static final Logger LOGGER = Logger.getLogger(RequestHandler.class.getName());

	private SeleniumRenderer seleniumRenderer;
	private RequestLogger logger;
	private ContentCache cache;

	public RequestHandler() {
		cache = new ContentCache();
		seleniumRenderer = new SeleniumRenderer(cache);
		logger = new RequestLogger();
	}

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		long start = System.currentTimeMillis();
		response.setContentType("text/html; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);

		PrintWriter out = response.getWriter();

		final String requestUrl = request.getPathInfo().substring(1).replace("?_escaped_fragment_=", "");

		//LOGGER.log(Level.INFO, "Received a new request: " + target);

		if (!requestUrl.startsWith("http")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			if (!requestUrl.startsWith("favicon.ico")) {
				out.println("Expecting a URL starting with http at this stage, got:" + requestUrl);
				LOGGER.log(Level.FINER, "Responding with bad request for " + requestUrl);
			}

		} else {

			try {

				if (cache.contentExists(requestUrl)) {
					out.println(cache.getContent(requestUrl));
				} else {
					Future<String> content = seleniumRenderer.startRendering(requestUrl);
					out.println(content.get());
				}

			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				out.println("Failed to render " + requestUrl + " " + e.getMessage());
			}

		}

		logger.log(request, response, start);

		baseRequest.setHandled(true);
	}
}