package com.sparender;

import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.session.SessionHandler;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageBuilder;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestLogger {

	final Logger LOGGER = LoggerFactory.getLogger(RequestLogger.class);

	private final DateFormat dateTimeFormat = createRFC1123DateTimeFormatter();

	public static DateFormat createRFC1123DateTimeFormatter() {
		DateFormat dtf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
		dtf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dtf;
	}

	final GelfConfiguration config;
	final GelfTransport transport;

	static final String GRAYLOG_HOST = App.prop.get("graylog.host").toString();
	static final int GRAYLOG_PORT = Integer.valueOf(App.prop.get("graylog.port").toString());

	public RequestLogger() {
		this.config = new GelfConfiguration(new InetSocketAddress(GRAYLOG_HOST, GRAYLOG_PORT)).transport(GelfTransports.UDP).queueSize(512).connectTimeout(5000).reconnectDelay(1000).tcpNoDelay(true)
				.sendBufferSize(32768);

		this.transport = GelfTransports.create(config);
	}

	public void log(HttpServletRequest request, HttpServletResponse response, long startTime){

		try {
			
			long elapsedTime = System.currentTimeMillis() - startTime;

			String remoteClient = request.getRemoteAddr();
			String method = request.getMethod();
			String uri = request.getRequestURI();
			new SessionHandler();

			Map<String, Object> keyValueFields = new HashMap<>();
			int contentLength = response.getBufferSize();

			String versionFormatted = "-";
			
			int status = response.getStatus();
			String message = null;

			String referrer = request.getHeader("referrer");
			String userAgent = request.getHeader("user-agent");
			referrer = referrer == null ? "-" : referrer;
			userAgent = userAgent == null ? "-" : userAgent;

			message = String.format("%s - - [%s] \"%s %s %s\" %d %d  - %d ms \"%s\" \"%s\"", remoteClient, dateTimeFormat.format(new Date(System.currentTimeMillis())), method, uri, versionFormatted, status, contentLength,
					elapsedTime, referrer, userAgent);

			keyValueFields.put("remoteClient", remoteClient);
			keyValueFields.put("method", method);
			keyValueFields.put("uri", uri);
			keyValueFields.put("versionFormatted", versionFormatted);
			keyValueFields.put("contentLength", contentLength);
			keyValueFields.put("referrer", referrer);
			keyValueFields.put("userAgent", userAgent);
			keyValueFields.put("elpasedTime", elapsedTime);
			keyValueFields.put("hitCache", (elapsedTime< SeleniumRenderer.TIME_TO_WAIT_FOR_RENDER));

			keyValueFields.put("status", status);

			if (status >= 500) {
				LOGGER.error(message);
				keyValueFields.put("level", "error");
			} else if (status >= 400) {
				LOGGER.warn(message);
				keyValueFields.put("level", "warning");
			} else {
				LOGGER.info(message);
				keyValueFields.put("level", "info");
			}

			final GelfMessageBuilder builder = new GelfMessageBuilder(message, "vertx.selenium.server");
			final GelfMessage gm = builder.additionalFields(keyValueFields).build();
			transport.trySend(gm);

		}catch (Exception e){
			e.printStackTrace();
		}

	}
}
