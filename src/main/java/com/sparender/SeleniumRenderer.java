package com.sparender;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple HTTP Server that renders HTML pages using Selenium.
 * 
 */
public class SeleniumRenderer implements Renderer {

	private static final Integer TIME_TO_WAIT_FOR_RENDER = 2000;
	private static final int POOL_MAX_SIZE = Integer.parseInt(App.prop.get("driver.pool.max"));
	private static final Logger LOGGER = LoggerFactory.getLogger(RequestLogger.class);

	private final ObjectPool<RemoteWebDriver> driverPool;

	public SeleniumRenderer() {

		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(POOL_MAX_SIZE);
		driverPool = new GenericObjectPool<>(new WebDriverFactory(), config);
	}

	@Override
	public String render(final String requestedUrl) throws Exception {

		RemoteWebDriver webDriver = null;

		try {

			String baseUrl = SparenderUtils.getBaseUrl(requestedUrl);
			final long start = System.currentTimeMillis();

			LOGGER.info("Trying to borrow a driver from the pool to render " + requestedUrl);
			webDriver = driverPool.borrowObject();
			LOGGER.info("Got the web driver " + webDriver.getSessionId());

			webDriver.get(requestedUrl);

			sleep(TIME_TO_WAIT_FOR_RENDER);

			LOGGER.info("Selenium finished rendering " + requestedUrl + " in " + (System.currentTimeMillis() - start)
					+ " ms");
			String content = updatePageSource(webDriver.getPageSource(), baseUrl);

			LOGGER.info("Returning driver " + webDriver.getSessionId() + " to the pool");
			driverPool.returnObject(webDriver);

			return content;
		} catch (Exception e) {

			if (webDriver != null) {

				LOGGER.error("Session " + webDriver.getSessionId() + " died: " + e.getMessage());
				driverPool.invalidateObject(webDriver);

				try {
					webDriver.close();
					webDriver.quit();
				} catch (Exception e2) {
					LOGGER.error(
							"Fails to properly close session " + webDriver.getSessionId() + ": " + e2.getMessage());
				}
				return render(requestedUrl);
			}

			throw e;
		}
	}

	private static String updatePageSource(String content, String baseUrl) {

		content = content.replaceAll("<script(.|\n)*?</script>", "");
		content = content.replaceAll("<link rel=\"import\".*/>", "");
		content = content.replaceAll("<iframe .*</iframe>","");
		content = content.replaceAll("href=\"/","href=\"" +  baseUrl + "/");
		content = content.replaceAll("href=\'/","href=\'" +  baseUrl + "/");
		
		return content.replaceAll("(<base.*?>)", "<base href=\"" + baseUrl + "\"/>");
	}

	private static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
