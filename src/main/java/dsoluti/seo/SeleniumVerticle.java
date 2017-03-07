package dsoluti.seo;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * Simple HTTP Server that renders HTML pages using Selenium.
 * 
 */
public class SeleniumVerticle extends AbstractVerticle {

	public static final Integer TIME_TO_WAIT_FOR_RENDER = 2000;
	static final String SELENIUM_URL = App.prop.get("selenium.url");

	@Override
    public void start(Future<Void> startFuture) {

        vertx.eventBus().consumer("render", message -> {

        	String base = "https://www.nextprot.org";
        	String requestedUrl = message.body().toString();
        			
    		if (ContentCache.contentExists(base, message.body().toString())) {
    			message.reply(ContentCache.getContent(base, requestedUrl));
    		}

    		WebDriver driver = null;
    		URL hubUrl = null;

    		try {
    			hubUrl = new URL(SELENIUM_URL);
    		} catch (MalformedURLException e1) {
    			e1.printStackTrace();
    			System.exit(1);
    		}

    		driver = new RemoteWebDriver(hubUrl, DesiredCapabilities.chrome());
    		driver.get(requestedUrl);
    		sleep(1000);

    		try {
    			// Waits for active connections to finish
    			(new WebDriverWait(driver, 50, 1000)).until(new ExpectedCondition<Boolean>() {
    				public Boolean apply(WebDriver d) {
    					//TODO only works with jQuery now, should be optimised
    					Object o = ((JavascriptExecutor) d).executeScript("return ((jQuery)? jQuery.active : 0)");
    					return o.equals(0L);
    				}
    			});

    		} catch (org.openqa.selenium.TimeoutException timeout) {
    			System.err.println("Not finished ... after timeout !!! ");
    		}

    		// TODO what to do when no response from docker / selenium (it blocks)
    		sleep(TIME_TO_WAIT_FOR_RENDER);

    		String content = driver.getPageSource();

    		String contentWithoutJs = content.replaceAll("<script(.|\n)*?</script>", "");
    		String contentWithoutJsAndHtmlImport = contentWithoutJs.replaceAll("<link rel=\"import\".*/>", "");
    		String contentWithCorrectBase = contentWithoutJsAndHtmlImport.replaceAll("(<base.*?>)", "<base href=\"" + base + "\"/>");

    		String finalContent = contentWithCorrectBase;

    		driver.quit();

    		ContentCache.putContent(base, requestedUrl, finalContent);
    		message.reply(finalContent);
			
        });
        
    }
	
	
	private static void sleep(long ms) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
