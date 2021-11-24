package com.asset.migration.config;


import com.asset.migration.util.IJsonAdapter;
import com.asset.migration.util.JacksonAdapter;
import lombok.Data;

import java.util.List;

@Data
public class MigrationConfig implements IMigrationConfig {

    List<TableConfig> tables;

    @Data
    public static class TableConfig {
        String from;
        String to;
        List<IColumnConfig> columns;
        List<IWhereConfig> where;

        public TableConfig(){}

        public String toString(){
            IJsonAdapter jsonAdapter = new JacksonAdapter();
            return jsonAdapter.writeValueAsString(this);

        }
    }


}
