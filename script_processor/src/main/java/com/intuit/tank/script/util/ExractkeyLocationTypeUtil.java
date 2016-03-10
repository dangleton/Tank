package com.intuit.tank.script.util;

import org.apache.commons.lang.StringUtils;

import com.intuit.tank.vm.api.enumerated.DataLocation;

/**
 * 
 * 
 * This method is used to parse either the key or location type. Currently the location type is stored as part of the
 * key in the following format: locationType:Key
 * 
 * @author alfredom
 */
public class ExractkeyLocationTypeUtil {
    /**
     * Extracts the Key portion from key
     * @param key
     * @return
     */
    public static String getKey(String key) {
        // Format ":key1"
        String ret = key;
        

        if (!StringUtils.isBlank(key)) {
            int index = key.indexOf(":");

            if (index > -1 && key.length() != 1) {
                if (isValidLoationType(key.substring(0, index)) || StringUtils.isEmpty(key.substring(0, index))) {
                    ret = key.substring(index + 1, key.length());
                }
            }
        }
        return ret;

    }

    /**
     * Extracts the Location Type portion from the key
     * @param locationType
     * @return
     */
    public static String getLocationType(String locationType) {

        String ret = DataLocation.Body.name();

        if (StringUtils.isNotBlank(locationType)) {
            int index = locationType.indexOf(":");

            if (index != -1) {
                String locationString = locationType.substring(0, index);
                if (isValidLoationType(locationString)) {
                    ret = locationString;
                }
            }
        }
        return ret;
    }

    /**
     * Checks to see if Location Type is a Valid DataLocation type
     * @param locationString
     * @return
     */
    private static boolean isValidLoationType(String locationString) {
        boolean ret = false;

        for (DataLocation d : DataLocation.values()) {
            if (d.name().equals(locationString)) {
                ret = true;
                break;
            }
        }
        return ret;
    }
}