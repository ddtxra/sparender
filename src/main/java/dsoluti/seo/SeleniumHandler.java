package dsoluti.seo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class SeleniumHandler extends AbstractHandler implements Handler {
	SeleniumRenderer seleniumRenderer;

	public SeleniumHandler() {
		seleniumRenderer = new SeleniumRenderer();
	}

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		response.setContentType("text/html; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);

		PrintWriter out = response.getWriter();

		final String requestUrl = request.getPathInfo().substring(1).replace("?_escaped_fragment_=", "");
		
		System.err.println("Received a new request: " + requestUrl);

		if (!requestUrl.startsWith("http")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			if(!requestUrl.startsWith("favicon.ico")){
				out.println("Expecting a URL starting with http at this stage, got:" + requestUrl);
				System.err.println("Responding with bad request for " + requestUrl);
			}

		} else {

			try {
				
				if (ContentCache.contentExists(SeleniumRenderer.base, requestUrl)) {
					out.println(ContentCache.getContent(SeleniumRenderer.base, requestUrl));
				}else {
					Future<String> content = seleniumRenderer.startRendering(requestUrl);
					out.println(content.get());
				}
				
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				out.println("Failed to render " + requestUrl + " " + e.getMessage());
			}

		}

		baseRequest.setHandled(true);
	}
}