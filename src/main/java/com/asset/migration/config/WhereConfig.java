package com.asset.migration.config;

import com.asset.migration.enums.Operator;
import lombok.Data;

import java.util.List;
@Data
public class WhereConfig implements IWhereConfig{
    Operator operator;
    List<ICondition> conditions;
}
