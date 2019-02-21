package com.rh.core.util.device;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * 获取终端类型工具类
 * @author wanglong
 */
public class DeviceUtils {

    /**
     * 手机终端UserAgent前缀
     */
    private static final String[] KNOWN_MOBILE_USER_AGENT_PREFIXES = new String[] { "w3c ", "w3c-", "acs-", "alav",
            "alca", "amoi", "audi", "avan", "benq", "bird", "blac", "blaz", "brew", "cell", "cldc", "cmd-", "dang",
            "doco", "eric", "hipt", "htc_", "inno", "ipaq", "ipod", "jigs", "kddi", "keji", "leno", "lg-c", "lg-d",
            "lg-g", "lge-", "lg/u", "maui", "maxo", "midp", "mits", "mmef", "mobi", "mot-", "moto", "mwbp", "nec-",
            "newt", "noki", "palm", "pana", "pant", "phil", "play", "port", "prox", "qwap", "sage", "sams", "sany",
            "sch-", "sec-", "send", "seri", "sgh-", "shar", "sie-", "siem", "smal", "smar", "sony", "sph-", "symb",
            "t-mo", "teli", "tim-", "tosh", "tsm-", "upg1", "upsi", "vk-v", "voda", "wap-", "wapa", "wapi", "wapp",
            "wapr", "webc", "winw", "xda ", "xda-" };

    /**
     * 手机终端UserAgent关键字
     */
    private static final String[] KNOWN_MOBILE_USER_AGENT_KEYWORDS = new String[] { "blackberry", "webos", "ipod",
            "lge vx", "midp", "maemo", "mmp", "mobile", "netfront", "hiptop", "nintendo DS", "novarra", "openweb",
            "opera mobi", "opera mini", "palm", "psp", "phone", "smartphone", "symbian", "up.browser", "up.link",
            "wap", "windows ce" };

    /**
     * 平板终端UserAgent关键字
     */
    private static final String[] KNOWN_TABLET_USER_AGENT_KEYWORDS = new String[] { "ipad", "playbook", "hp-tablet",
            "kindle" };
    
    /**
     * 手机终端UserAgent前缀列表
     */
    private static final List<String> MOBILE_USER_AGENT_PREFIXES = new ArrayList<String>();

    /**
     * 手机终端UserAgent关键字列表
     */
    private static final List<String> MOBILE_USER_AGENT_KEYWORDS = new ArrayList<String>();

    /**
     * 平板终端UserAgent关键字列表
     */
    private static final List<String> TABLET_USER_AGENT_KEYWORDS = new ArrayList<String>();

    /**
     * 桌面终端UserAgent关键字列表
     */
    private static final List<String> DESKTOP_USER_AGENT_KEYWORDS = new ArrayList<String>();
    
    static {
        MOBILE_USER_AGENT_PREFIXES.addAll(Arrays.asList(KNOWN_MOBILE_USER_AGENT_PREFIXES));
        MOBILE_USER_AGENT_KEYWORDS.addAll(Arrays.asList(KNOWN_MOBILE_USER_AGENT_KEYWORDS));
        TABLET_USER_AGENT_KEYWORDS.addAll(Arrays.asList(KNOWN_TABLET_USER_AGENT_KEYWORDS));
    }

    /**
     * session里终端KEY
     */
    public static final String CURRENT_DEVICE_ATTRIBUTE = "currentDevice";

    /**
     * 获取终端对象
     * @param request HttpServletRequest
     * @return 返回终端对象
     */
    public static Device getCurrentDevice(HttpServletRequest request) {
        return getCurrentDevice(request, false);
    }
    
    /**
     * 获取终端对象
     * @param request HttpServletRequest
     * @param rewrite 是否重新计算
     * @return 返回终端对象
     */
    public static Device getCurrentDevice(HttpServletRequest request, boolean rewrite) {
        Device currentDevice = null;
        if (rewrite) { // 重新计算
            currentDevice = resolveDevice(request);
            request.setAttribute(CURRENT_DEVICE_ATTRIBUTE, currentDevice);
        } else { // 先从request里取，取不到则重新计算
            currentDevice = (Device) request.getSession().getAttribute(CURRENT_DEVICE_ATTRIBUTE);
            if (currentDevice == null) {
                currentDevice = getCurrentDevice(request, true);
            }
        }
        return currentDevice;
    }
    
    /**
     * 获取终端对象
     * @param request HttpServletRequest
     * @param normalUserAgentKeywords 桌面终端关键字
     * @return 返回终端对象
     */
    public static Device getCurrentDevice(HttpServletRequest request, List<String> normalUserAgentKeywords) {
        if (normalUserAgentKeywords != null && normalUserAgentKeywords.size() > 0) {
            DESKTOP_USER_AGENT_KEYWORDS.addAll(normalUserAgentKeywords);
        }
        return getCurrentDevice(request, true);
    }
    
    /**
     * 解析终端类型
     * @param request HttpServletRequest
     * @return 返回终端类型
     */
    public static Device resolveDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        // UserAgent keyword detection of Normal devices
        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();
            for (String keyword : DESKTOP_USER_AGENT_KEYWORDS) {
                if (userAgent.contains(keyword)) {
                    return Device.DESKTOP_INSTANCE;
                }
            }
        }
        // UAProf detection
        if (request.getHeader("x-wap-profile") != null || request.getHeader("Profile") != null) {
            return Device.MOBILE_INSTANCE;
        }
        // UserAgent prefix detection
        if (userAgent != null && userAgent.length() >= 4) {
            String prefix = userAgent.substring(0, 4).toLowerCase();
            if (MOBILE_USER_AGENT_PREFIXES.contains(prefix)) {
                return Device.MOBILE_INSTANCE;
            }
        }
        // Accept-header based detection
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("wap")) {
            return Device.MOBILE_INSTANCE;
        }
        // UserAgent keyword detection for Mobile and Tablet devices
        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();
            // Android special case 
            if (userAgent.contains("android") && !userAgent.contains("mobile")) {
                return Device.TABLET_INSTANCE;
            }
            // Kindle Fire special case 
            if (userAgent.contains("silk") && !userAgent.contains("mobile")) {
                return Device.TABLET_INSTANCE;
            }
            for (String keyword : TABLET_USER_AGENT_KEYWORDS) {
                if (userAgent.contains(keyword)) {
                    return Device.TABLET_INSTANCE;
                }
            }
            for (String keyword : MOBILE_USER_AGENT_KEYWORDS) {
                if (userAgent.contains(keyword)) {
                    return Device.MOBILE_INSTANCE;
                }
            }
        }
        // OperaMini special case
        @SuppressWarnings("rawtypes")
        Enumeration headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = (String) headers.nextElement();
            if (header.contains("OperaMini")) {
                return Device.MOBILE_INSTANCE;
            }
        }
        return Device.DESKTOP_INSTANCE;
    }

}
