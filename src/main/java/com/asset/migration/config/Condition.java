package com.asset.migration.config;

import com.asset.migration.enums.ComparisonOperator;
import lombok.Data;

@Data
public class Condition implements ICondition {
    ComparisonOperator comparison;
    String column;
    String value;
    String dataType;
}
