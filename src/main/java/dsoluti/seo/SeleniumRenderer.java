package dsoluti.seo;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Simple HTTP Server that renders HTML pages using Selenium.
 * 
 */
public class SeleniumRenderer {

	public static final Integer TIME_TO_WAIT_FOR_RENDER = 2000;

	private DriverPool driverPool;
	private final ExecutorService pool = Executors.newFixedThreadPool(3);
	public static String base = "https://www.nextprot.org";
	
	public SeleniumRenderer(){
		System.err.println("Starting driver pool");
		driverPool = new DriverPool(3);
		System.err.println("Pool started");
	}

	public Future<String> startRendering(final String requestedUrl) throws IOException {
		return pool.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {

				if (!ContentCache.contentExists(base, requestedUrl)) {

					final long start = System.currentTimeMillis();

					WebDriver driver = driverPool.borrowObject();
					System.err.println("Starting to get the page " + (System.currentTimeMillis() - start) + " ms");
					driver.get(requestedUrl);
					System.err.println("Time to get the page " + (System.currentTimeMillis() - start) + " ms");

					sleep(1000);

					/*try {
						// Waits for active connections to finish
						(new WebDriverWait(driver, 50, 1000)).until(new ExpectedCondition<Boolean>() {
							public Boolean apply(WebDriver d) {
								System.err.println("Waiting since " + (System.currentTimeMillis() - start) + " ms");
								// TODO only works with jQuery now, should be
								// optimised
								Object o = ((JavascriptExecutor) d).executeScript("return ((jQuery)? jQuery.active : 0)");
								return o.equals(0L);
							}
						});

					} catch (org.openqa.selenium.TimeoutException timeout) {
						System.err.println("Not finished ... after timeout !!! ");
					}*/

					String content = driver.getPageSource();

					String contentWithoutJs = content.replaceAll("<script(.|\n)*?</script>", "");
					String contentWithoutJsAndHtmlImport = contentWithoutJs.replaceAll("<link rel=\"import\".*/>", "");
					String contentWithCorrectBase = contentWithoutJsAndHtmlImport.replaceAll("(<base.*?>)",
							"<base href=\"" + base + "\"/>");

					String finalContent = contentWithCorrectBase;

					driverPool.returnObject(driver);

					System.err.println("Finished in " + (System.currentTimeMillis() - start) + " ms");

					ContentCache.putContent(base, requestedUrl, finalContent);
				}

				
				return ContentCache.getContent(base, requestedUrl);

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

}
