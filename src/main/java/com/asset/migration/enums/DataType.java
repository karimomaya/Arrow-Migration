package com.asset.migration.enums;

public enum DataType {
    VARCHAR("varchar"), NUMBER("number"), DATE("date"), DATETIME("datetime");

    private String dataType;

    private DataType(final String dataType) {
        this.dataType = dataType;
    }

    public String getDataType() {
        return dataType;
    }
}
