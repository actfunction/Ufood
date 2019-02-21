package com.rh.core.util.device;

/**
 * 终端封装类
 * @author wanglong
 */
public class Device {
    
    /**
     * 桌面终端单例
     */
    public static final Device DESKTOP_INSTANCE = new Device(DeviceType.DESKTOP);

    /**
     * 手机终端单例
     */
    public static final Device MOBILE_INSTANCE = new Device(DeviceType.MOBILE);

    /**
     * 平板终端单例
     */
    public static final Device TABLET_INSTANCE = new Device(DeviceType.TABLET);
    
    /**
     * 终端类型枚举
     * @author wanglong
     */
    public enum DeviceType {
        
        /**
         * 桌面终端
         */
        DESKTOP, 
        
        /**
         * 手机终端
         */
        MOBILE, 
        
        /**
         * 平板终端
         */
        TABLET
    }
    
    /**
     * @return 桌面终端
     */
    public boolean isNormal() {
        return this.deviceType == DeviceType.DESKTOP;
    }

    /**
     * @return 手机终端
     */
    public boolean isMobile() {
        return this.deviceType == DeviceType.MOBILE;
    }

    /**
     * @return 平板终端
     */
    public boolean isTablet() {
        return this.deviceType == DeviceType.TABLET;
    }

    /**
     * @return 终端类型
     */
    public DeviceType getDeviceType() {
        return this.deviceType;
    }
    
    /**
     * @return toString
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[Device ");
        builder.append("type").append("=").append(this.deviceType);
        builder.append("]");
        return builder.toString();
    }

    private final DeviceType deviceType;

    /**
     * 创建指定终端类型
     * @param deviceType 终端类型
     */
    private Device(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

}