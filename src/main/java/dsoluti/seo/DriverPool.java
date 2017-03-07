package dsoluti.seo;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DriverPool extends ObjectPool<WebDriver> {
	static final String SELENIUM_URL = App.prop.get("selenium.url");

	public DriverPool(int minIdle) {
		super(minIdle);
	}

	protected WebDriver createObject() {
		URL hubUrl;
		try {
			hubUrl = new URL(SELENIUM_URL);
			return new RemoteWebDriver(hubUrl, DesiredCapabilities.chrome());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}