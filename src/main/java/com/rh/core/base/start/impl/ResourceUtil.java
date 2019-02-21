package com.rh.core.base.start.impl;

import java.util.ResourceBundle;

public class ResourceUtil {
    private static final ResourceBundle resourceBundle;

    static{
        resourceBundle = ResourceBundle.getBundle("properties.configs");
    }

    public static String getKey(String key){
       return resourceBundle.getString(key);
    }
}
