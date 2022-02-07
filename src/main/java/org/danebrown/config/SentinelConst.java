package org.danebrown.config;

/**
 * Created by danebrown on 2021/2/27
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
public class SentinelConst {
    public static final String OBJECT_KEY="object_key";
    private static String USE_SENTINEL="USE_SENTINEL";
    public static boolean isUseSentinel = Boolean.parseBoolean(System.getProperty(
            USE_SENTINEL,
            "false"));
}
