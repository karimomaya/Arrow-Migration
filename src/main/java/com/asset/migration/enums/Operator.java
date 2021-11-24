package com.asset.migration.enums;

public enum Operator {
    AND("and"), OR("or"), NOT("not");

    private String operator;

    private Operator(final String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
}
