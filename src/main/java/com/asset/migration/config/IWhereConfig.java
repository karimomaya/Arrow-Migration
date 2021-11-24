package com.asset.migration.config;

import com.asset.migration.enums.Operator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

@JsonDeserialize(as = WhereConfig.class)
public interface IWhereConfig {
    public List<ICondition> getConditions();
    public Operator getOperator();
}
