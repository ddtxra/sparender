package com.sparender;

import java.net.MalformedURLException;
import java.net.URL;

public class SparenderUtils {

	public static String getBaseUrl(String string) {
		try {

			URL url = new URL(string);
			if(url.getPort() == -1){
				return url.getProtocol() + "://" + url.getHost();
			}else {
				return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
			}

		} catch (MalformedURLException e) {
			throw new RuntimeException("Requested url does not matched expected url.");
		}

	}
}
