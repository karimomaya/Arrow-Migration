package com.asset.migration.query;

import com.asset.migration.enums.Operator;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Map;

public interface IMigrationQuery {
    public Map<String, IColumnQuery> getColumnsMapping();
    public void setResult(List<Tuple> results);
    public List<Tuple> getResult();
    public void setSelectQuery(String query);
    public void setToQuery(String query);
    public String getToQuery();
    public String getSelectQuery();
    public String getFromTable();
    public String getToTable();
    public void setToTable(String toTable);
    public void setFromTable(String fromTable);
    public IColumnQuery setColumnsMapping(String from, IColumnQuery columnQuery);
    public void setConditionList(Operator operator, List<String> conditionList);
    public List<MigrationQuery.ConditionQuery> getConditionList();
}
