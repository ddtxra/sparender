package dsoluti.seo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class ContentCache {

	private Cache htmlCache;
	private CacheManager cm;

	public ContentCache() {
		cm = CacheManager.newInstance();
		htmlCache = cm.getCache("html-cache");
		addShutdownHook();
	}

	public boolean contentExists(String url) {
		return htmlCache.isKeyInCache(url);
	}

	public String getContent(String url) {
		Element element = htmlCache.get(url);
		return unzip((byte[]) element.getObjectValue());
	}

	public void putContent(String url, String content) {
		htmlCache.put(new Element(url, zip(content)));
	}

	private byte[] zip(String stringToCompress) {

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

	private String unzip(byte[] bytes) {

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
	
	
	private void addShutdownHook() {
		Thread localShutdownHook = new Thread() {
			@Override
			public void run() {
				synchronized (this) {
					cm.shutdown();
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(localShutdownHook);
	}

	

}