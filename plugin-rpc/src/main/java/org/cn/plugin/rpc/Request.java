package org.cn.plugin.rpc;

import java.util.HashMap;
import java.util.Map;

public class Request {
	public final long beginTime = System.currentTimeMillis();
	public final String url;
	public final String method;
	public final String body;
	public final int retry;
	public String contentType = "application/json; charset=UTF-8";
	public final Map<String, String> header;

	private Request(Builder builder) {
		this.url = builder.url;
		this.method = builder.method;
		this.body = builder.body;
		this.retry = builder.retry;
		if (builder.contentType != null) {
			this.contentType = builder.contentType;
		}
		this.header = builder.header;
	}

	public String url() {
		return url;
	}

	public String method() {
		return method;
	}

	public String body() {
		return body;
	}

	public int retry() {
		return retry;
	}

	public static Builder create() {
		return new Builder();
	}

	public static class Builder {
		public String url;
		public String method;
		public String body;
		public int retry = 0;
		public String contentType;
		public Map<String, String> header = new HashMap<>();

		public Builder get(String url) {
			this.url = url;
			this.method = "GET";
			return this;
		}

		public Builder post(String url) {
			this.url = url;
			this.method = "POST";
			return this;
		}

		public Builder url(String url) {
			return get(url);
		}

		public Builder body(String body) {
			this.body = body;
			return this;
		}

		public Builder retry(int retry) {
			this.retry = retry;
			return this;
		}

		public void contentType(String contentType) {
			this.contentType = contentType;
		}

		public Builder addHeader(String key, String body) {
			header.put(key, body);
			return this;
		}

		public Request build() {
			if (url == null) {
				throw new IllegalStateException("url must not be null.");
			}
			return new Request(this);
		}
	}
}
