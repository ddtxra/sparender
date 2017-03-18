package com.sparender;

import java.io.IOException;
import java.net.URL;

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

	static final String SELENIUM_URL = App.prop.get("selenium.url");
	public static String base = "https://www.nextprot.org";

	final Logger LOGGER = LoggerFactory.getLogger(RequestLogger.class);

	public String render(final String requestedUrl) throws IOException {

		WebDriver webdriver = null;
		try {

			LOGGER.info("Starting to render" + requestedUrl);

			final long start = System.currentTimeMillis();

			webdriver = new RemoteWebDriver(new URL(SELENIUM_URL), DesiredCapabilities.chrome());

			LOGGER.info("Got the driver for " + requestedUrl);

			webdriver.get(requestedUrl);
			LOGGER.info("Finished to driver.get for " + requestedUrl);

			sleep(TIME_TO_WAIT_FOR_RENDER);

			/*
			 * try { // Waits for active connections to finish (new
			 * WebDriverWait(driver, 50, 1000)).until(new
			 * ExpectedCondition<Boolean>() { public Boolean apply(WebDriver d)
			 * { System.err.println("Waiting since " +
			 * (System.currentTimeMillis() - start) + " ms"); // TODO only works
			 * with jQuery now, should be // optimised Object o =
			 * ((JavascriptExecutor)
			 * d).executeScript("return ((jQuery)? jQuery.active : 0)"); return
			 * o.equals(0L); } });
			 * 
			 * } catch (org.openqa.selenium.TimeoutException timeout) {
			 * System.err.println("Not finished ... after timeout !!! " ); }
			 */

			String content = webdriver.getPageSource();

			String contentWithoutJs = content.replaceAll("<script(.|\n)*?</script>", "");
			String contentWithoutJsAndHtmlImport = contentWithoutJs.replaceAll("<link rel=\"import\".*/>", "");
			String contentWithoutJsAndHtmlImportAndIframes = contentWithoutJsAndHtmlImport
					.replaceAll("<iframe .*</iframe>", "");
			String contentWithCorrectBase = contentWithoutJsAndHtmlImportAndIframes.replaceAll("(<base.*?>)",
					"<base href=\"" + base + "\"/>");

			String finalContent = contentWithCorrectBase;

			LOGGER.info("Finished rendering " + requestedUrl + " in " + (System.currentTimeMillis() - start) + " ms");

			return finalContent;

		} finally {

			if (webdriver != null) {
				webdriver.quit();
			}
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
