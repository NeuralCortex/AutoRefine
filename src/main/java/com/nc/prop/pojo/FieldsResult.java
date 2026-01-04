package com.nc.prop.pojo;

import java.util.Set;

/**
 *
 * @author Neural Cortex
 */
public class FieldsResult {

    private final Set<String> usedFields;
    private final Set<String> usedEnums;

    public FieldsResult(Set<String> usedFields, Set<String> usedEnums) {
        this.usedFields = usedFields;
        this.usedEnums = usedEnums;
    }

    public Set<String> getUsedFields() {
        return usedFields;
    }

    public Set<String> getUsedEnums() {
        return usedEnums;
    }

}
