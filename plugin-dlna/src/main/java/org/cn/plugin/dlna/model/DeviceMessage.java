package org.cn.plugin.dlna.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;

/**
 * Created by chenning on 17-1-13.
 */

public class DeviceMessage implements Serializable {
    public String id;
    public String body;

    public String getMessage(String key) {
        if (key == null || key.length() <= 0) {
            return null;
        }
        if (body == null || body.length() <= 0) {
            return null;
        }
        StringReader sr = new StringReader(body);
        BufferedReader br = new BufferedReader(sr);

        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                if (line.toUpperCase().startsWith(key.toUpperCase() + ":")) {
                    return line.substring(key.length() + 1).trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
