package org.cn.plugin.rpc;

public interface ResponseListener<T> {
	void onResponse(T response);
}
