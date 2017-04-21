package org.cn.plugin.airkiss;

import java.util.Map;

public interface AirKiss {

    void execute(Map<String, String> map);

    void cancel();

    void registerListener(AirKissListener listener);

    void unregisterListener(AirKissListener listener);
}
