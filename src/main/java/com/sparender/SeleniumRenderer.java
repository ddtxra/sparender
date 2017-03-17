package com.sparender;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple HTTP Server that renders HTML pages using Selenium.
 * 
 */
public class SeleniumRenderer {

	public static final Integer TIME_TO_WAIT_FOR_RENDER = 2000;

	static final int DRIVER_POOL = Integer.valueOf(App.prop.get("driver.pool"));
	private DriverPool driverPool;
	
	private final ExecutorService pool = Executors.newFixedThreadPool(DRIVER_POOL);
	public static String base = "https://www.nextprot.org";
	private ContentCache cache;

	final Logger LOGGER = LoggerFactory.getLogger(RequestLogger.class);

	public SeleniumRenderer(ContentCache cache) {
		driverPool = new DriverPool(DRIVER_POOL);
		System.err.println("Pool started with " + DRIVER_POOL + " drivers");
		this.cache = cache;
		addShutdownHook();
	}

	public Future<String> startRendering(final String requestedUrl) throws IOException {

		LOGGER.info("Requesting to render" + requestedUrl + " ms");

		return pool.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {

				if (!cache.contentExists(requestedUrl)) {

					LOGGER.info("Starting to render" + requestedUrl);

					final long start = System.currentTimeMillis();

					WebDriver driver = driverPool.borrowObject();
					driver.get(requestedUrl);

					sleep(1000);

					/*
					 * try { // Waits for active connections to finish (new
					 * WebDriverWait(driver, 50, 1000)).until(new
					 * ExpectedCondition<Boolean>() { public Boolean
					 * apply(WebDriver d) { System.err.println("Waiting since "
					 * + (System.currentTimeMillis() - start) + " ms"); // TODO
					 * only works with jQuery now, should be // optimised Object
					 * o = ((JavascriptExecutor)
					 * d).executeScript("return ((jQuery)? jQuery.active : 0)");
					 * return o.equals(0L); } });
					 * 
					 * } catch (org.openqa.selenium.TimeoutException timeout) {
					 * System.err.println("Not finished ... after timeout !!! "
					 * ); }
					 */

					String content = driver.getPageSource();

					String contentWithoutJs = content.replaceAll("<script(.|\n)*?</script>", "");
					String contentWithoutJsAndHtmlImport = contentWithoutJs.replaceAll("<link rel=\"import\".*/>", "");
					String contentWithoutJsAndHtmlImportAndIframes = contentWithoutJsAndHtmlImport.replaceAll("<iframe .*</iframe>", "");
					String contentWithCorrectBase = contentWithoutJsAndHtmlImportAndIframes.replaceAll("(<base.*?>)", "<base href=\"" + base + "\"/>");

					String finalContent = contentWithCorrectBase;

					driverPool.returnObject(driver);

					LOGGER.info("Finished rendering " + requestedUrl + " in " + (System.currentTimeMillis() - start) + " ms");

					cache.putContent(requestedUrl, finalContent);
				}

				return cache.getContent(requestedUrl);

			}
		});
	}

	private static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void addShutdownHook() {
		Thread localShutdownHook = new Thread() {
			@Override
			public void run() {
				driverPool.shutdown();
			}
		};
		Runtime.getRuntime().addShutdownHook(localShutdownHook);
	}

}
