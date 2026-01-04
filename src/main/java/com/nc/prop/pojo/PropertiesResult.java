package com.nc.prop.pojo;

import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author Neural Cortex
 */
public class PropertiesResult {

    private final HashMap<String, Set<Object>> map;
    private final Set<String> usedKeys;

    public PropertiesResult(HashMap<String, Set<Object>> map, Set<String> usedKeys) {
        this.map = map;
        this.usedKeys = usedKeys;
    }

    public HashMap<String, Set<Object>> getMap() {
        return map;
    }

    public Set<String> getUsedKeys() {
        return usedKeys;
    }
}
