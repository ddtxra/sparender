package dsoluti.seo;


/*
public class GelfLoggerHandler implements LoggerHandler {

	static final String GRAYLOG_HOST = App.prop.get("graylog.host").toString();
	static final int GRAYLOG_PORT = Integer.valueOf(App.prop.get("graylog.port").toString());

	final GelfConfiguration config;
	final GelfTransport transport;
	private final io.vertx.core.logging.Logger logger = LoggerFactory.getLogger(this.getClass());
	private final DateFormat dateTimeFormat = Utils.createRFC1123DateTimeFormatter();
	private final boolean immediate;
	private final LoggerFormat format;

	public GelfLoggerHandler(boolean immediate, LoggerFormat format) {
		this.immediate = immediate;
		this.format = format;
		this.config = new GelfConfiguration(new InetSocketAddress(GRAYLOG_HOST, GRAYLOG_PORT)).transport(GelfTransports.UDP).queueSize(512).connectTimeout(5000).reconnectDelay(1000).tcpNoDelay(true)
				.sendBufferSize(32768);
		this.transport = GelfTransports.create(config);
		System.err.println("Configuring to send messages to " + GRAYLOG_HOST + ":" + GRAYLOG_PORT);

	}

	public GelfLoggerHandler(LoggerFormat format) {
		this(false, format);
	}

	public GelfLoggerHandler() {
		this(false, LoggerFormat.DEFAULT);
	}

	private String getClientAddress(SocketAddress inetSocketAddress) {
		if (inetSocketAddress == null) {
			return null;
		}
		return inetSocketAddress.host();
	}

	private void log(RoutingContext context, long timestamp, String remoteClient, HttpVersion version, HttpMethod method, String uri) {
		
		Map<String, Object> keyValueFields = new HashMap<>();
		
		HttpServerRequest request = context.request();
		long contentLength = 0;
		if (immediate) {
			Object obj = request.headers().get("content-length");
			if (obj != null) {
				try {
					contentLength = Long.parseLong(obj.toString());
				} catch (NumberFormatException e) {
					// ignore it and continue
					contentLength = 0;
				}
			}
		} else {
			contentLength = request.response().bytesWritten();
		}
		String versionFormatted = "-";
		switch (version) {
		case HTTP_1_0:
			versionFormatted = "HTTP/1.0";
			break;
		case HTTP_1_1:
			versionFormatted = "HTTP/1.1";
			break;
		}
		
		int status = request.response().getStatusCode();
		String message = null;

		switch (format) {
		case DEFAULT:
			String referrer = request.headers().get("referrer");
			String userAgent = request.headers().get("user-agent");
			referrer = referrer == null ? "-" : referrer;
			userAgent = userAgent == null ? "-" : userAgent;

			message = String.format("%s - - [%s] \"%s %s %s\" %d %d  - %d ms \"%s\" \"%s\"", remoteClient, dateTimeFormat.format(new Date(timestamp)), method, uri, versionFormatted, status, contentLength,
					(System.currentTimeMillis() - timestamp), referrer, userAgent);
			
			keyValueFields.put("remoteClient", remoteClient);
			keyValueFields.put("method", method);
			keyValueFields.put("uri", uri);
			keyValueFields.put("versionFormatted", versionFormatted);
			keyValueFields.put("contentLength", contentLength);
			keyValueFields.put("referrer", referrer);
			keyValueFields.put("userAgent", userAgent);
			keyValueFields.put("elpasedTime", (System.currentTimeMillis() - timestamp));
			keyValueFields.put("hitCache", ((System.currentTimeMillis() - timestamp) < SeleniumRenderer.TIME_TO_WAIT_FOR_RENDER));
			
			break;
		case SHORT:
			message = String.format("%s - %s %s %s %d %d - %d ms", remoteClient, method, uri, versionFormatted, status, contentLength, (System.currentTimeMillis() - timestamp));
			break;
		case TINY:
			message = String.format("%s %s %d %d - %d ms", method, uri, status, contentLength, (System.currentTimeMillis() - timestamp));
			break;
		}
		doLog(status, message, keyValueFields);
	}

	protected void doLog(int status, String message, Map<String, Object> keyValueFields) {

		keyValueFields.put("status", status);

		if (status >= 500) {
			logger.error(message);
			keyValueFields.put("level", "error");
		} else if (status >= 400) {
			logger.warn(message);
			keyValueFields.put("level", "warning");
		} else {
			logger.info(message);
			keyValueFields.put("level", "info");
		}

		final GelfMessageBuilder builder = new GelfMessageBuilder(message, "vertx.selenium.server");
		final GelfMessage gm = builder.additionalFields(keyValueFields).build();
		boolean enqueued = transport.trySend(gm);

	}

	@Override
	public void handle(RoutingContext context) {
		// common logging data
		long timestamp = System.currentTimeMillis();
		String remoteClient = getClientAddress(context.request().remoteAddress());
		HttpMethod method = context.request().method();
		String uri = context.request().uri();
		HttpVersion version = context.request().version();

		if (immediate) {
			log(context, timestamp, remoteClient, version, method, uri);
		} else {
			context.addBodyEndHandler(v -> log(context, timestamp, remoteClient, version, method, uri));
		}

		context.next();

	}
}
*/