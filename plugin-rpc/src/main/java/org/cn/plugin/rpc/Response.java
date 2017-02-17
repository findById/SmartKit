package org.cn.plugin.rpc;

public class Response {
	public int statusCode;
	public String message;
	public long timestamp;
	public String result;

	public boolean isSuccess() {
		return statusCode == 0 || statusCode == 200;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append('"').append("statusCode").append('"');
		sb.append(":").append(statusCode).append(",");
		sb.append('"').append("message").append('"');
		sb.append(":\"").append(message).append("\",");
		sb.append('"').append("timestamp").append('"');
		sb.append(":").append(timestamp).append(",");
		sb.append('"').append("result").append('"');
		sb.append(":").append(result);
		sb.append("}");
//		return "Response [statusCode=" + statusCode + ", description=" + description + ", timestamp=" + timestamp
//				+ ", response=" + response + "]";
		return sb.toString();
	}

}
