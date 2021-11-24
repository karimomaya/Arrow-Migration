package com.asset.migration.config;

import com.asset.migration.enums.Datasource;
import com.asset.migration.query.IBSValidation;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
@JsonDeserialize(as = ColumnConfig.class)
public interface IColumnConfig {
    public String getFrom();
    public String getTo();
    public String getDataType();
    public String getConfig();
    public String getId();
    public String getDefaultVal();
    public List<IBSValidation> getBsValidations();
    public Datasource getDatasource();
    public String getQuery();
    public Boolean getSkipTo();
    public Boolean getSkipFrom();
}
