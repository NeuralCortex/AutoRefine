package com.nc.prop.pojo;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Neural Cortex
 */
public class PropertiesInfo {

    private final Set<String> usedKeys=new HashSet<>();
    private final Set<String> propertyKeys=new HashSet<>();

    public Set<String> getUsedKeys() {
        return usedKeys;
    }

    public Set<String> getPropertyKeys() {
        return propertyKeys;
    }
}
