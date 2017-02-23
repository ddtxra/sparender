package dsoluti.seo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebRemoteDriver {

	static Cache<String, byte[]> htmlCache;
	
	static {
		
		CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
			    .with(CacheManagerBuilder.persistence("cache"))
			    .withCache("htmlCache", CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class, ResourcePoolsBuilder.newResourcePoolsBuilder()
			                .disk(100, MemoryUnit.GB, true)))
			        .build(true);

		htmlCache = cacheManager.getCache("htmlCache", String.class, byte[].class);
	}
	
	public static String getContent(String base, String requestedUrl){
		
		if(htmlCache.containsKey(base + requestedUrl)){
			return unzip(htmlCache.get(base + requestedUrl));
		}
		
		WebDriver driver = null;
		URL hubUrl = null;

		try {
			hubUrl = new URL("http://dockerdev.vital-it.ch:32768/wd/hub");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			System.exit(1);
		}

		driver = new RemoteWebDriver(hubUrl, DesiredCapabilities.chrome());
		driver.get(requestedUrl);
		sleep(1000);
		
		try {
			//Waits for active connections to finish
			(new WebDriverWait(driver, 50, 1000)).until(new ExpectedCondition<Boolean>() {
	            public Boolean apply(WebDriver d) {
	        		Object o = ((JavascriptExecutor) d).executeScript("return ((jQuery)? jQuery.active : 0)");
	            	return o.equals(0L);
	            }
	        });
			
		}catch (org.openqa.selenium.TimeoutException timeout) {
			System.err.println("Not finished ... after timeout !!! ");
		}
		
		//TODO what to do when no response from docker / selenium (it blocks)
	
		System.err.println("Finished waiting");

		sleep(2000);
		
		String content = driver.getPageSource();

		String contentWithoutJs = content.replaceAll("<script(.|\n)*?</script>", "");
		String contentWithoutJsAndHtmlImport = contentWithoutJs.replaceAll("<link rel=\"import\".*/>", "");
		String contentWithCorrectBase = contentWithoutJsAndHtmlImport.replaceAll("(<base.*?>)", "<base href=\"" + base + "\"/>");
		
		String bootstrapScript = "\n<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\"></script>";
		String finalContent = contentWithCorrectBase + "\n" + bootstrapScript;
		
		driver.quit();

		htmlCache.put(base + requestedUrl, zip(finalContent));
		return finalContent;

	}
	
	private static void sleep(long ms){
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static byte[] zip(String stringToCompress){
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzipOut;
		try {
			gzipOut = new GZIPOutputStream(baos);
			ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);
			objectOut.writeObject(stringToCompress);
			objectOut.close();
			return baos.toByteArray();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String unzip(byte[] bytes){
		
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		try {
			GZIPInputStream gzipIn = new GZIPInputStream(bais);
			ObjectInputStream objectIn = new ObjectInputStream(gzipIn);
			String myObj2 = (String) objectIn.readObject();
	  	    objectIn.close();
			return myObj2;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}


}