package com.asset.migration.enums;

public enum Datasource {
    PRIMARY("primaryEntityManager"), SECONDARY("secondEntityManager");

    private String datasource;

    private Datasource(final String datasource) {
        this.datasource = datasource;
    }

    public String getDatasource() {
        return datasource;
    }
}
