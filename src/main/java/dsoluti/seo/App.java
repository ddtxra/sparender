package dsoluti.seo;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;

/**
 * Simple HTTP Server that renders HTML pages using Selenium.
 * 
 */
public class App {

	public static void main(String[] args) {

		VertxOptions vertxOptions = new VertxOptions();
		vertxOptions.setMaxEventLoopExecuteTime(60000000000L); // 20 seconds

		HttpServer server = Vertx.vertx(vertxOptions).createHttpServer();

		server.requestHandler(request -> {

			String regex = "^(?!.*?(\\.js|\\.css|\\.xml|\\.less|\\.png|\\.jpg|\\.jpeg|\\.gif|\\.pdf|\\.doc|\\.txt|\\.ico|\\.rss|\\.zip|\\.mp3|\\.rar|\\.exe|\\.wmv|\\.doc|\\.avi|\\.ppt|\\.mpg|\\.mpeg|\\.tif|\\.wav|\\.mov|\\.psd|\\.ai|\\.xls|\\.mp4|\\.m4a|\\.swf|\\.dat|\\.dmg|\\.iso|\\.flv|\\.m4v|\\.torrent|\\.ttf|\\.woff))(.*)";

			//String base = "https://www.nextprot.org";
			String base = "https://bed-search.nextprot.org";

			String requestedUrl = base +  request.path();
			
			System.out.println("Asking for " + requestedUrl + " and path is " + request.path());
			
			if(requestedUrl.matches(regex)){

				System.out.println("Responding with selenium");

				String url =  request.absoluteURI().replaceAll("http://localhost:8082", base);
				String content = WebRemoteDriver.getContent(url);
				request.response().putHeader("content-type", "text/html").end(content);


			}else {

				System.out.println("Responding with redirect" + requestedUrl);
				request.response().putHeader("location", requestedUrl).setStatusCode(302).end();
			}




		});

		Integer port = 8082;
		server.listen(port);
		System.out.println("Listening to port " + port);

	}

}
