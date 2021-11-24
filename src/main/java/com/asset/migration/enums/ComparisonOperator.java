package com.asset.migration.enums;

public enum ComparisonOperator {
    GTE(">="),
    GT(">"),
    LTE("<="),
    LT("<"),
    EQ("="),
    NOT("!="),
    IN("in"),
    BETWEEN("between"),
    LIKE("like");

    private String operator;

    private ComparisonOperator(final String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
}
