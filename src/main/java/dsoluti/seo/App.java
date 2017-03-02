package dsoluti.seo;

/**
 * Main class used for debug / IDE
 * Launch Verticle with docker instead (try this for debug)
 * See documentation here: http://vertx.io/docs/vertx-docker/#_deploying_a_java_verticle_in_a_docker_container
 * 
 * @author Daniel Teixeira http://github.com/ddtxra
 *
 */
public class App {
	
	public static void main(String[] args) throws Exception {
	
		VertxSeleniumServer server = new VertxSeleniumServer();
		server.start();
		
	}


}