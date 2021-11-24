package com.asset.migration.config;

import com.asset.migration.enums.ComparisonOperator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = Condition.class)
public interface ICondition {
    public String getColumn();
    public String getValue();
    public String getDataType();
    public ComparisonOperator getComparison();
}
