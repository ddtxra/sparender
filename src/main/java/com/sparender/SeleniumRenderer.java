package com.sparender;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple HTTP Server that renders HTML pages using Selenium.
 * 
 */
public class SeleniumRenderer {

	public static final Integer TIME_TO_WAIT_FOR_RENDER = 2000;

	static final int DRIVER_POOL = Integer.valueOf(App.prop.get("driver.pool"));
	static final String SELENIUM_URL = App.prop.get("selenium.url");

	private final ExecutorService pool = Executors.newFixedThreadPool(DRIVER_POOL);
	public static String base = "https://www.nextprot.org";
	private ContentCache cache;

	final Logger LOGGER = LoggerFactory.getLogger(RequestLogger.class);

	public SeleniumRenderer(ContentCache cache) {
		this.cache = cache;
	}

	public Future<String> requestARendering(final String requestedUrl) throws IOException {
	
		return  pool.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {

				if (!cache.contentExists(requestedUrl)) {

					WebDriver webdriver = null;
					try {

						LOGGER.info("Starting to render" + requestedUrl);

						final long start = System.currentTimeMillis();

						webdriver = new RemoteWebDriver(new URL(SELENIUM_URL), DesiredCapabilities.chrome());

						LOGGER.info("Got the driver for " + requestedUrl);

						webdriver.get(requestedUrl);
						LOGGER.info("Finished to driver.get for " + requestedUrl);

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

						String content = webdriver.getPageSource();

						String contentWithoutJs = content.replaceAll("<script(.|\n)*?</script>", "");
						String contentWithoutJsAndHtmlImport = contentWithoutJs.replaceAll("<link rel=\"import\".*/>", "");
						String contentWithoutJsAndHtmlImportAndIframes = contentWithoutJsAndHtmlImport.replaceAll("<iframe .*</iframe>", "");
						String contentWithCorrectBase = contentWithoutJsAndHtmlImportAndIframes.replaceAll("(<base.*?>)", "<base href=\"" + base + "\"/>");

						String finalContent = contentWithCorrectBase;


						LOGGER.info("Finished rendering " + requestedUrl + " in " + (System.currentTimeMillis() - start) + " ms");

						cache.putContent(requestedUrl, finalContent);

					}finally {
						
						if(webdriver != null){
							webdriver.close();
						}
					}
				}

				return cache.getContent(requestedUrl);

			}
		});

	}

		
		
	public String startRendering(final String requestedUrl) throws IOException, InterruptedException, ExecutionException {

		if (cache.contentExists(requestedUrl)) {
			//Should not happen because it was checked before
			LOGGER.info("Page was on cache " + requestedUrl);
			return cache.getContent(requestedUrl);
			
		} else {
			
			LOGGER.info("Requesting to render" + requestedUrl + " ms");
			Future<String> future = requestARendering(requestedUrl);
			return future.get();
			
		}


	}

	private static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}


}
