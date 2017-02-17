package org.cn.plugin.rpc.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by chenning on 16-11-30.
 */

public class IOUtil {

    public static long copy(InputStream is, OutputStream os) throws IOException {
        return copy(is, os, new byte[1024 * 8]);
    }

    public static long copy(InputStream is, OutputStream os, byte[] buffer) throws IOException {
        try {
            long total = 0;
            int len = 0;
            while ((len = is.read(buffer)) >= 0) {
                os.write(buffer, 0, len);
                total += len;
            }
            return total;
        } finally {
            closeQuietly(is, os);
        }
    }

    public static String asString(InputStream is, String charset) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            copy(is, baos);
            return baos.toString(charset);
        } finally {
            closeQuietly(is, baos);
        }
    }

    public static void closeQuietly(Closeable... closeables) {
        if (closeables != null && closeables.length > 0) {
            for (Closeable ac : closeables) {
                if (ac != null) {
                    try {
                        ac.close();
                        ac = null;
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
    }

}
