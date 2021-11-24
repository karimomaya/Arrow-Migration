package com.asset.migration.query;

import com.asset.migration.enums.Datasource;

import java.util.List;

public interface IColumnQuery {
    public IColumnQuery setName(String columnName);
    public String getName();
    public String getConfig();
    public String getQuery();
    public IColumnQuery setDataType(String dataType);
    public String getDataType();
    public Boolean getSkipTo();
    public Boolean getSkipFrom();
    public IColumnQuery setConfig(String config);
    public IColumnQuery setSkipTo(Boolean skip);
    public IColumnQuery setSkipFrom(Boolean skip);
    public IColumnQuery setQuery(String query);
    public IColumnQuery setDatasource(Datasource datasource);
    public Datasource getDatasource();
    public IColumnQuery setId(String id);
    public IColumnQuery setDefaultVal(String defaultVal);
    public String getDefaultVal();
    public IColumnQuery setBsValidations(List<IBSValidation> bsValidations);
}
