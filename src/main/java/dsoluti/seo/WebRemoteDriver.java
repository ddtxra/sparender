package dsoluti.seo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebRemoteDriver {
	
	private static Map<String, String> cache = new HashMap<>();
	
	public static String getContent(String requestedUrl){
		
		if(cache.containsKey(requestedUrl)){
			return cache.get(requestedUrl);
		}
		
		WebDriver driver = null;
		URL hubUrl = null;
		System.err.println("Starting ");

		try {
			hubUrl = new URL("http://localhost:4444/wd/hub");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			System.exit(1);
		}

		driver = new RemoteWebDriver(hubUrl, DesiredCapabilities.chrome());

		driver.get(requestedUrl);
		sleep(100);

		
		try {
			//Waits for active connections to finish
			(new WebDriverWait(driver, 50, 1000)).until(new ExpectedCondition<Boolean>() {
	            public Boolean apply(WebDriver d) {
	        		Object o = ((JavascriptExecutor) d).executeScript("return jQuery.active;");
	        		System.out.println(o.getClass());
	        		System.err.println(o);
	            	return o.equals(0L);
	            }
	        });
		}catch (org.openqa.selenium.TimeoutException timeout) {
			System.err.println("Not finished ... after timeout !!! ");
		}
		
		//TODO what to do when no response from docker / selenium (it blocks)
	
		System.err.println("Finished waiting");

		sleep(1000);
		
		String content = driver.getPageSource();

		String contentWithoutJs = content.replaceAll("<script(.|\n)*?</script>", "");
		String contentWithoutJsAndHtmlImport = contentWithoutJs.replaceAll("<link rel=\"import\".*/>", "");
		
		String jqueryScript = "\n<script src=\"https://code.jquery.com/jquery-3.1.1.min.js\"></script>";
		String bootstrapScript = "\n<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\"></script>";
		String highchartScript = "\n<script src=\"https://rawgit.com/calipho-sib/nextprot-ui/develop/vendor/scripts/highstock.src.js\"></script>";
		String finalContent = contentWithoutJsAndHtmlImport + "\n" + highchartScript + jqueryScript + bootstrapScript;
		
		driver.quit();

		cache.put(requestedUrl, finalContent);
		return finalContent;

	}
	
	private static void sleep(long ms){
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	

}