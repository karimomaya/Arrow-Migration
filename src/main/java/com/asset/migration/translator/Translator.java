package com.asset.migration.translator;

import com.asset.migration.config.*;
import com.asset.migration.enums.ComparisonOperator;
import com.asset.migration.enums.Datasource;
import com.asset.migration.execution.IQueryExecutable;
import com.asset.migration.query.ColumnQuery;
import com.asset.migration.query.IColumnQuery;
import com.asset.migration.query.IMigrationQuery;
import com.asset.migration.query.MigrationQuery;
import com.asset.migration.util.DeepCopy;
import com.asset.migration.util.JacksonAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import javax.persistence.Tuple;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;


@Slf4j
public class Translator implements ITranslator{
    Environment env;
    IQueryExecutable queryExecutable;

    public Translator(Environment env, IQueryExecutable queryExecutable){
        this.env = env;
        this.queryExecutable = queryExecutable;
    }

    private String constructFromQuery(IMigrationQuery migrationQuery){
        StringBuilder selectStatement = new StringBuilder();

        selectStatement.append("SELECT ");
        int count = 0;
        for (Map.Entry entry : migrationQuery.getColumnsMapping().entrySet()) {
            if (entry.getKey().toString().contains("SKIP_") || ((IColumnQuery)entry.getValue()).getSkipFrom()) continue;
            if (count++ >0 ) selectStatement.append(",");
            selectStatement.append(entry.getKey().toString() + " ");
        }

        selectStatement.append("FROM "+migrationQuery.getFromTable());

        List<MigrationQuery.ConditionQuery> conditionQueryList = migrationQuery.getConditionList();

        if (conditionQueryList.size() == 0) return selectStatement.toString();

        selectStatement.append(" WHERE ");

        conditionQueryList.stream().forEach(condition->{

            List conditions = condition.getConditionList();
            IntStream.range(0, conditions.size()).forEach(i->{

                if (i>0) selectStatement.append( " " + condition.getOperator().getOperator() + " ");
                selectStatement.append(conditions.get(i));
            });

        });
        log.info(selectStatement.toString());
        return selectStatement.toString();
    }


    public List<IMigrationQuery> translate(List<IMigrationQuery> migrationQueries){
        migrationQueries.stream().forEach(migrationQuery -> {
            String toQuery = constructInsertionQuery(migrationQuery);
            log.info(toQuery);
            migrationQuery.setToQuery(toQuery);
        });
        return migrationQueries;
    }

    /**
     * ConstructInsertionQuery insertion query consist of three Steps Insertion Head, Insertion Body, Insertion Values (or insertion tail)
     * Insertion Head focuses on which table to insert
     * @see Translator#constructInsertionHead(IMigrationQuery)
     * The Insertion Body focus on which columns to insert into
     * @see Translator#constructInsertionBody(IMigrationQuery)
     * The insertion values focus on what value will be inserted where will have BS validation and set default values. or handling lookups
     * @see Translator#constructInsertionValues(IMigrationQuery, String)
     * @example INSERT INTO table (id, name) Values (1, 'Ahmed');
     * @param migrationQuery
     * @return Insert Queries String
     */
    private String constructInsertionQuery(IMigrationQuery migrationQuery){
        String insertionHead = constructInsertionHead(migrationQuery);
        String insertionBody = constructInsertionBody(migrationQuery);
        String insertStatement = constructInsertionValues(migrationQuery, insertionHead.toString() + " " + insertionBody);
        return insertStatement;
    }

    /**
     * ConstructInsertionHead focus on which table to insert
     * @example INSERT INTO table
     * @param migrationQuery
     * @return
     */
    private String constructInsertionHead(IMigrationQuery migrationQuery){
        StringBuilder insertStatement = new StringBuilder();
        insertStatement.append("INSERT INTO ");
        insertStatement.append(migrationQuery.getToTable());

        return insertStatement.toString();
    }

    /**
     * ConstructInsertionBody focus on which columns to insert onto
     * @example (id, name)
     * @param migrationQuery
     * @return
     */
    private String constructInsertionBody(IMigrationQuery migrationQuery){
        StringBuilder insertStatement = new StringBuilder("(");
        int count = 0;
        for (Map.Entry entry : migrationQuery.getColumnsMapping().entrySet()) {
            if(((IColumnQuery)entry.getValue()).getSkipTo()) continue;
            if (count++ >0 ) insertStatement.append(",");
            insertStatement.append(((IColumnQuery)entry.getValue()).getName() + " ");
        }
        insertStatement.append(")");
        return insertStatement.toString();
    }

    /**
     * ConstructInsertionValues focus on what value will be inserted.
     * and also check validation rules and set default values, and handle lookups if exist
     * @example (1, 'Ahmed')
     * @param migrationQuery
     * @param mainInsert
     * @return
     */
    private String constructInsertionValues(IMigrationQuery migrationQuery, String mainInsert){
        StringBuilder insertStatement = new StringBuilder();
        migrationQuery.getResult().forEach(tuple -> {
            StringBuilder stringBuilder = new StringBuilder(mainInsert);
            stringBuilder.append(" VALUES (");
            int count = 0;
            for (Map.Entry entry : migrationQuery.getColumnsMapping().entrySet()) {
                IColumnQuery columnQuery = (IColumnQuery)entry.getValue();
                if(columnQuery.getSkipTo()) continue;

                String val = getValue(tuple, entry, migrationQuery);
                if (count++ >0 ) stringBuilder.append(",");
                stringBuilder.append(val);
            }
            stringBuilder.append("); \n");
            insertStatement.append(stringBuilder);
        });

        return insertStatement.toString();
    }

    private String getValue(Tuple tuple, Map.Entry entry, IMigrationQuery migrationQuery){
        IColumnQuery columnQuery = (IColumnQuery)entry.getValue();

        Object value = getDefaultValue(tuple, entry);

        value = getValueFromConfig(value, columnQuery);

        value = getValueFromQuery(columnQuery, tuple, value, entry.getKey(), migrationQuery);

        return translateValue((IColumnQuery)entry.getValue(), value);
    }

    private Object getValueFromQuery(IColumnQuery columnQuery, Tuple tuple, Object value, Object key, IMigrationQuery migrationQuery){
        Object val = value;
        Datasource datasource = columnQuery.getDatasource();
        if (columnQuery.getQuery() != null){
            String query = columnQuery.getQuery();
            Pattern p = Pattern.compile("\\$\\b(\\w*)");
            Matcher m = p.matcher(query);
            while (m.find()) {
                String id = m.group(1).replace("$", "");
                if (id.equals(key)) val = value;
                else {
                    IColumnQuery columnQueryMapper = migrationQuery.getColumnsMapping().get(id);
                    val = getDefaultValue(tuple, columnQueryMapper, id);
                    val = getValueFromConfig(val, columnQuery);
                }
                System.out.println(m.group(1));
                query = query.replace("$"+m.group(1), val.toString());
            }

            List<Tuple> tupleList = queryExecutable.executeQuery(query,datasource);
            val = tupleList.get(0).get(0);
        }
        return val;
    }


    private Object getDefaultValue(Tuple tuple, IColumnQuery columnQuery, String key){

        Object value = "";

        if (!columnQuery.getSkipFrom())  value = tuple.get(key);



        if ((value == null || value.toString().isEmpty()) && columnQuery.getDefaultVal() != null) {
            value = columnQuery.getDefaultVal();
            if (value.equals("random")) {
                value  = (columnQuery.getDataType() != null && columnQuery.getDataType().toLowerCase().equals("number"))? generateRandomInt() : generateRandomString();
            }
        }

        return value;
    }

    private int generateRandomInt(){
        UUID idOne = UUID.randomUUID();
        String str=""+idOne;
        int uid=str.hashCode();
        String filterStr=""+uid;
        str=filterStr.replaceAll("-", "");
        return Integer.parseInt(str);
    }

    private String generateRandomString(){
        return UUID.randomUUID().toString();
    }

    private Object getDefaultValue(Tuple tuple, Map.Entry entry){
        IColumnQuery columnQuery = (IColumnQuery)entry.getValue();
        return getDefaultValue(tuple, columnQuery, entry.getKey().toString());
    }

    private Object getValueFromConfig(Object val, IColumnQuery columnQuery){

        if (columnQuery.getConfig() != null){
            JacksonAdapter jacksonAdapter = new JacksonAdapter();
            try {
                Map<?, ?> map =jacksonAdapter.convert(new File(columnQuery.getConfig()), Map.class);
                val = map.get(val);

            } catch (IOException e) {
                log.error("getValueFromConfig "+ e.getMessage());
                e.printStackTrace();
            }

        }
        return val;
    }

    private String translateValue(IColumnQuery columnQuery, Object val){

        String castFormat = env.getProperty("app.query.cast." + columnQuery.getDataType().toLowerCase());
        if (castFormat.equals("app.query.cast.date")){
            val = val.toString().trim().split(" ")[0];
        }else if (castFormat.equals("app.query.cast.datetime")){
            val = val.toString().trim().split("\\.")[0];
        }
        castFormat= String.format(castFormat, val);
        return castFormat;
    }

    @Override
    public List<IMigrationQuery> translate(IMigrationConfig migrationConfig){
        List<IMigrationQuery> migrationQueries = new ArrayList<>();
        migrationConfig.getTables().stream().forEach((tableConfig -> {
            log.info("Start Migrate table ("+ tableConfig.getFrom() + ") -> table (" + tableConfig.getTo()+")");
            IMigrationQuery migrationQuery = new MigrationQuery();

            migrationQuery = translateTable(tableConfig, migrationQuery);
            migrationQuery = translateColumns(tableConfig.getColumns(), migrationQuery);
            migrationQuery = translateWhere(tableConfig.getWhere(), migrationQuery);

            migrationQuery.setSelectQuery(constructFromQuery(migrationQuery));


            log.info(migrationQuery.getSelectQuery());


            migrationQueries.add(migrationQuery);

        }));
        return migrationQueries;
    }

    private IMigrationQuery translateWhere(List<IWhereConfig> whereConfigs, IMigrationQuery migrationQuery){
        whereConfigs.stream().forEach(whereConfig -> {
            List conditionList = translateConditions(whereConfig.getConditions(), migrationQuery);
            migrationQuery.setConditionList(whereConfig.getOperator(), conditionList);
        });
        return migrationQuery;
    }

    private List translateConditions(List<ICondition> conditions, IMigrationQuery migrationQuery){
        List<String> conditionList = new ArrayList<>();
        conditions.stream().forEach(condition ->{
            String conditionStr = translateCondition(condition);
            conditionList.add(conditionStr);
        });

        return conditionList;
    }

    private String translateCondition(ICondition condition){
        String result = "";
        result = constructCondition(condition.getColumn(), condition.getValue(), condition.getDataType(), condition.getComparison());
        return result;
    }

    /**
     * constructCondition function used to construct one condition without the WHERE keyword and the AND, OR operators.
     * and it also cast the value with its dataType
     * @example id > 0
     * @param column
     * @param value
     * @param dataType
     * @param comparisonOperator
     * @return String query
     * @throws ClassNotFoundException
     */
    public String constructCondition(String column, String value, String dataType, ComparisonOperator comparisonOperator) {

        StringBuilder result = new StringBuilder();
        result.append(column);

        String castFormat = env.getProperty("app.query.cast." + dataType.toLowerCase());
        castFormat= String.format(castFormat, value);

        result.append(" "+comparisonOperator.getOperator() +" ");
        result.append(castFormat);

        return result.toString();

    }


    private IMigrationQuery translateColumns(List<IColumnConfig> columnConfigs, IMigrationQuery migrationQuery){
        columnConfigs.stream().forEach((columnConfig -> {

            IColumnQuery columnQuery = transform(columnConfig);

            if (columnConfig.getFrom() == null) columnQuery = migrationQuery.setColumnsMapping("SKIP_"+columnConfig.getTo(), columnQuery);
            else if (columnConfig.getSkipFrom() ) columnQuery = migrationQuery.setColumnsMapping(columnConfig.getFrom(), columnQuery);
            else columnQuery = migrationQuery.setColumnsMapping(columnConfig.getFrom(), columnQuery);
            if (columnConfig.getFrom() != null && !columnConfig.getSkipFrom())
            log.info("Map Column ("+ columnConfig.getFrom() + ") -> Map Column (" + columnConfig.getTo()+")");

        }));
        return migrationQuery;
    }


    private IColumnQuery transform(IColumnConfig columnConfig){

        DeepCopy deepCopy = new DeepCopy<IColumnConfig, IColumnQuery>(columnConfig, new ColumnQuery());

        deepCopy.setException("getTo", "setName");

        IColumnQuery columnQuery = (IColumnQuery) deepCopy.copy();


        return columnQuery;
    }


    private IMigrationQuery translateTable(MigrationConfig.TableConfig tableConfig, IMigrationQuery migrationQuery){
        String fromTable = tableConfig.getFrom();
        String toTable = tableConfig.getTo();

        migrationQuery.setToTable(toTable);
        migrationQuery.setFromTable(fromTable);

        return migrationQuery;
    }
}
