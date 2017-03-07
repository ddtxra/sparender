package dsoluti.seo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;

public class ContentCache {

	static Cache<String, byte[]> htmlCache;
	static {

		CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().with(CacheManagerBuilder.persistence("cache")).withCache("htmlCache",
				CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class, ResourcePoolsBuilder.newResourcePoolsBuilder().disk(100, MemoryUnit.GB, true))).build(true);

		htmlCache = cacheManager.getCache("htmlCache", String.class, byte[].class);
		
	}

	public static boolean contentExists(String base, String requestedUrl) {
		return htmlCache.containsKey(base + requestedUrl);
	}

	public static String getContent(String base, String requestedUrl) {
		return unzip(htmlCache.get(base + requestedUrl));
	}
	
	public static void putContent(String base, String requestedUrl, String content) {
		htmlCache.put((base + requestedUrl), zip(content));
	}

	private static byte[] zip(String stringToCompress) {

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

	private static String unzip(byte[] bytes) {

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