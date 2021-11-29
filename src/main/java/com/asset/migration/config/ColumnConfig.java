package com.asset.migration.config;

import com.asset.migration.enums.Datasource;
import com.asset.migration.query.IBSValidation;
import lombok.Data;

import java.util.List;
@Data
public class ColumnConfig implements IColumnConfig{
    String from;
    String to;
    String config;
    String query;
    String dataType;
    Datasource datasource;
    String id;
    String defaultVal;
    List<IBSValidation> bsValidations;
    boolean skipTo = false;
    boolean skipFrom = false;
    public ColumnConfig(){};
    public Boolean getSkipTo(){
        return this.skipTo;
    }
    public Boolean getSkipFrom(){
        return this.skipFrom;
    }
}
