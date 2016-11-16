package com.bouilli.nxx.bouillihotel.util;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Created by 18230 on 2016/10/29.
 * 读取properties文件工具类
 */

public class PropertiesUtil {

    public static String getPropertiesURL(String propName, Context c, String s) {
        String url = null;
        Properties properties = new Properties();
        try {
            properties.load(c.getAssets().open(propName));
            url = properties.getProperty(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }
}
