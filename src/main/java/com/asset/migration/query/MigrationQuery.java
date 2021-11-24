package com.asset.migration.query;

import com.asset.migration.config.IColumnConfig;
import com.asset.migration.enums.ComparisonOperator;
import com.asset.migration.enums.DataType;
import com.asset.migration.enums.Operator;
import com.asset.migration.util.DeepCopy;
import lombok.Data;

import javax.persistence.Tuple;
import java.util.*;

/**
 * MigrationQuery
 */

@Data
public class MigrationQuery implements IMigrationQuery {

    private String fromTable;
    private String toTable;
    private Map<String, IColumnQuery> columnsMapping;
    private List<ConditionQuery> conditionList;
    private String selectQuery;
    private String toQuery;
    private List<Tuple> result;

    public MigrationQuery(){}

    /**
     * Set Conditions into List; evey item in the list is ConditionQuery Class this class hold multiple condition and one operator that will be applied on
     * the condition list. the example below demonstrate one item in the list all of them will be with one operator AND
     * @example (id > 0 AND name != "User" AND password="123")
     * @param operator
     * @param conditionList
     * @see ConditionQuery
     */
    @Override
    public void setConditionList(Operator operator, List<String> conditionList){
        if (this.conditionList == null) this.conditionList = new ArrayList<>();
        ConditionQuery conditionQuery = new ConditionQuery();
        conditionQuery.setConditionList(conditionList);
        conditionQuery.setOperator(operator);
        this.conditionList.add(conditionQuery);
    }


    public String constructCondition(String column, String value, String dataType, ComparisonOperator comparisonOperator) throws ClassNotFoundException {
        StringBuilder result = new StringBuilder();
        result.append(column);
        Object val = cast(value, DataType.NUMBER);
        result.append(" "+comparisonOperator.getOperator() +" ");
        result.append(val);
        return result.toString();
    }

    /**
     * cast used to convert string val to its correct dataType
     * @param val
     * @param dataType
     * @return
     * @throws ClassNotFoundException
     */
    private Object cast(Object val, DataType dataType) throws ClassNotFoundException {
        switch (dataType) {
            case NUMBER: {
                return Integer.parseInt((String) val);
            }
            case VARCHAR: {
                String type = "java.lang.String";
                Class<? extends String> cls = Class.forName(type).asSubclass(String.class);
                return "'" + cls.cast(val) + "'";
            }
            case DATE: {
                String type = "java.util.Date";
                Class<? extends Date> cls = Class.forName(type).asSubclass(Date.class);
                return cls.cast(val);
            }
            default:
                return val;
        }
    }

    @Override
    public IColumnQuery setColumnsMapping(String from, IColumnQuery columnQuery){
        if (this.columnsMapping == null) columnsMapping = new LinkedHashMap<>();

        columnsMapping.put(from, columnQuery);
        return columnQuery;
    }

    public void setColumnsMapping(Map columnsMapping){
        this.columnsMapping = columnsMapping;
    }

    @Data
    public static class ConditionQuery {
        Operator operator;
        List<String> conditionList;
    }




}
