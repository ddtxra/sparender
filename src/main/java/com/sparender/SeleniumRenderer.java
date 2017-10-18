package com.sparender;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Simple HTTP Server that renders HTML pages using Selenium.
 * 
 */
public class SeleniumRenderer implements Renderer {

	private static final Integer TIME_TO_WAIT_FOR_RENDER = 5000;
	private static final int POOL_MAX_SIZE = Integer.parseInt(App.prop.get("driver.pool.max"));
	private static final Logger LOGGER = LoggerFactory.getLogger(RequestLogger.class);
	private final ExecutorService timeoutExecutorService = Executors.newFixedThreadPool(POOL_MAX_SIZE);

	private final ObjectPool<RemoteWebDriver> driverPool;

	public SeleniumRenderer() {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(POOL_MAX_SIZE);
		driverPool = new GenericObjectPool<>(new WebDriverFactory(), config);
	}

	@Override
	public String render(String requestedUrl) throws Exception {
		return render(requestedUrl, 1);
	}

	public String render(final String requestedUrl, int attemptCount) throws Exception {

		RemoteWebDriver webDriver = null;

		Thread.sleep(attemptCount * 1000);

		if(attemptCount > 3){
			LOGGER.error("Total disaster:  Reached the maximum number of attempts for " + requestedUrl + "returning JSON LD only");
			throw new RuntimeException("Total disaster:  Reached the maximum attempts for " + requestedUrl);
		}

		try {

			LOGGER.info("Trying to borrow a driver from the pool for ..." + requestedUrl + " attempt: " + attemptCount);
			webDriver = driverPool.borrowObject();
			LOGGER.info("Got the web driver " + webDriver.getSessionId());

			Callable<String> c = new TimeoutRenderer(webDriver, requestedUrl);

			//Should get the result within the given time. Otherwise a timeout is thrown and the driver invalidated.
			Future<String> future = timeoutExecutorService.submit(c);
			String content = future.get(8, TimeUnit.MINUTES);

			driverPool.returnObject(webDriver);

			return content;


		} catch (Exception e) {

			if (webDriver != null) {

				LOGGER.error("Session " + webDriver.getSessionId() + " died or was timeout : " + ExceptionUtils.getStackTrace(e));
				driverPool.invalidateObject(webDriver);
				LOGGER.error(webDriver.getSessionId() + " is not valid anymore");

				try {
					webDriver.quit();
				} catch (Exception e2) {
					LOGGER.error("Fails to properly destroy session " + webDriver.getSessionId() + ": " + ExceptionUtils.getStackTrace(e));
				}
				return render(requestedUrl, attemptCount + 1);
			}

			throw e;
		}
	}

	private static String updatePageSource(String content, String baseUrl) {

		//Remove any javascript content
		content = content.replaceAll("<script(.|\n)*?</script>", "");

		//Remove styling, really?
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

	private static class TimeoutRenderer implements Callable {

		private String requestedUrl;
		private RemoteWebDriver webDriver;
		private String baseUrl;

		TimeoutRenderer(RemoteWebDriver webDriver, String url){
			this.requestedUrl = url;
			this.webDriver = webDriver;
			this.baseUrl = SparenderUtils.getBaseUrl(requestedUrl);

		}

		@Override
		public String call() throws Exception {

			final long start = System.currentTimeMillis();

			LOGGER.info("Initializing the rendering process in another thread for " + requestedUrl + " in " + (System.currentTimeMillis() - start) + " ms for web driver " + webDriver.getSessionId());

			webDriver.get(requestedUrl);

			sleep(TIME_TO_WAIT_FOR_RENDER);

			LOGGER.info("Selenium finished rendering " + requestedUrl + " in " + (System.currentTimeMillis() - start) + " ms for web driver " + webDriver.getSessionId());

			String source = webDriver.getPageSource();

			LOGGER.info("Got page source for " + requestedUrl + " in " + (System.currentTimeMillis() - start) + " ms for web driver " + webDriver.getSessionId());

			String content = updatePageSource(source, this.baseUrl);

			LOGGER.info("Updating page source in " + (System.currentTimeMillis() - start) + " ms and returning web driver " + webDriver.getSessionId() + " to the pool");

			return content;
		}
	}

}
