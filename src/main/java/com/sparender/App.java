package com.sparender;

import org.eclipse.jetty.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Main application class to start sparender https://github.com/sparender
 *
 * @author Daniel Teixeira https://github.com/ddtxra
 *
 */
public class App {

	public static Map<String, String> prop = new HashMap<>();

	public static void main(String[] args) throws Exception {

		readPropertyFile();

		Server server = new Server(8082);
		server.setHandler(new RequestHandler());

		server.start();
		server.join();
	}

	private static void readPropertyFile() throws IOException {

		Path path = Paths.get("config.properties");

		try (BufferedReader br = Files.newBufferedReader(path)) {
			br.lines().filter(l -> !l.isEmpty()).filter(l -> !l.startsWith("#")).forEach(l -> {
				String[] keyValuePair = l.split("=");
				System.err.println(l);
				prop.put(keyValuePair[0], keyValuePair[1]);
			});
		}

	}
}