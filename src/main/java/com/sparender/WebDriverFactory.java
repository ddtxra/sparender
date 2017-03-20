package com.sparender;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spf4j.recyclable.ObjectCreationException;
import org.spf4j.recyclable.ObjectDisposeException;
import org.spf4j.recyclable.RecyclingSupplier;

public class WebDriverFactory implements RecyclingSupplier.Factory<WebDriver> {

	final Logger WebDriverFactory = LoggerFactory.getLogger(WebDriverFactory.class);
	static final String SELENIUM_URL = App.prop.get("selenium.url");
	final Logger LOGGER = LoggerFactory.getLogger(RequestLogger.class);

	
	@Override
	public WebDriver create() throws ObjectCreationException {
		try {
			LOGGER.info("Creating new driver");
			return new RemoteWebDriver(new URL(SELENIUM_URL), DesiredCapabilities.chrome());
		} catch (MalformedURLException e) {
			throw new ObjectCreationException(e);
		}
	}

	@Override
	public void dispose(WebDriver driver) throws ObjectDisposeException {
		if(driver != null){
			LOGGER.info("Disposing web driver");
			driver.close();
			driver.quit();
		}
	}

	@Override
	public boolean validate(WebDriver driver, Exception exception) throws Exception {
		if(exception != null){
			LOGGER.error("Validation failed, exception: " + exception.getMessage());
		}
		return (exception == null);
	}

}
