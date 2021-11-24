package com.asset.migration.query;

import com.asset.migration.enums.Datasource;
import lombok.Data;

import java.util.List;

@Data
public class ColumnQuery implements IColumnQuery {
    String name;
    String dataType;
    List<IBSValidation> bsValidations;
    String config;
    String query;
    Datasource datasource;
    String id;
    String defaultVal;
    Boolean skipTo;
    Boolean skipFrom;

    public IColumnQuery setName(String name) {
        this.name = name;
        return this;
    }

    public IColumnQuery setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    public IColumnQuery setBsValidations(List<IBSValidation> bsValidations) {
        this.bsValidations = bsValidations;
        return this;
    }

    public IColumnQuery setConfig(String config) {
        this.config = config;
        return this;
    }

    public IColumnQuery setQuery(String query) {
        this.query = query;
        return this;
    }

    @Override
    public IColumnQuery setDatasource(Datasource datasource) {
        this.datasource = datasource;
        return this;
    }

    public IColumnQuery setId(String id) {
        this.id = id;
        return this;
    }

    public IColumnQuery setDefaultVal(String defaultVal) {
        this.defaultVal = defaultVal;
        return this;
    }

    public IColumnQuery setSkipTo(Boolean skip){
        this.skipTo = skip;
        return this;
    }

    public IColumnQuery setSkipFrom(Boolean skip){
        this.skipFrom = skip;
        return this;
    }

    public Boolean getSkipTo(){
        return this.skipTo;
    }
    public Boolean getSkipFrom(){
        return this.skipTo;
    }


}
