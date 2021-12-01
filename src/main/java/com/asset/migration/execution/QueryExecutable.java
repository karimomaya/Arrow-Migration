package com.asset.migration.execution;

import com.asset.migration.enums.Datasource;
import com.asset.migration.query.IMigrationQuery;
import lombok.Data;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class QueryExecutable implements IQueryExecutable {
    private EntityManager primaryEntityManager;
    private EntityManager secondaryEntityManager;
    private IMigrationQuery migrationQuery;
    private Environment env;

    public QueryExecutable(){}
    public QueryExecutable(Environment env, EntityManager primaryEntityManager, EntityManager secondaryEntityManager){
        this.primaryEntityManager = primaryEntityManager;
        this.secondaryEntityManager = secondaryEntityManager;
        this.env = env;
    }
    public QueryExecutable(IMigrationQuery migrationQuery){
        this.migrationQuery = migrationQuery;
    }


    public List<IMigrationQuery> executeQueries(List<IMigrationQuery> migrationQueries){
        migrationQueries.stream().forEach(migrationQuery->{
            executeQuery(migrationQuery);
        });
        return migrationQueries;
    }

    public IMigrationQuery executeQuery(){
        if (this.migrationQuery == null) throw new RuntimeException("MigrationQuery is not defined ");
        return executeQuery(this.migrationQuery);
    }


    public void executeToQueries(List<IMigrationQuery> migrationQueries){
        migrationQueries.stream().forEach(migrationQuery->{
            String[] result = migrationQuery.getToQuery().split("\\r?\\n");
            String endStr = this.env.getProperty("migrate.batch.insertion.end");
            String startStr = this.env.getProperty("migrate.batch.insertion.start");
            String loopStr = this.env.getProperty("migrate.batch.insertion.loop");


            int resultLength = result.length;
            int start = (startStr != null && !startStr.isEmpty())? Integer.parseInt(startStr) : 0;;
            int end = (endStr != null && !endStr.isEmpty())? Integer.parseInt(endStr) : 100;
            int endAppender = end;

            end = (end>resultLength)? resultLength : end;

            while (end <= resultLength){

                try{
                    log.info("Start execute queries from: ("+start+") end: ("+end+")");
                    executeBatchInsertQueryUsingJdbcTemplate(Arrays.copyOfRange(result, start, end), Datasource.SECONDARY);
                    log.info("Insertion complete");
                }catch (RuntimeException exception){
                    log.error("Cannot execute insertion queries start from: ("+start+") end in: ("+end+") caused By: ("+ exception.getMessage()+")");
                }
                if (loopStr != null && !loopStr.isEmpty() && loopStr.equals("false")) break;

                start = end;
                end = end+endAppender;
                end = (end>resultLength)? resultLength : end;
            }

        });
    }

    public IMigrationQuery executeQuery(IMigrationQuery migrationQuery){
        List<Tuple> result = executeQuery(migrationQuery.getSelectQuery(), Datasource.PRIMARY);
        migrationQuery.setResult(result);
        return migrationQuery;
    }

    @Transactional
    public List<Tuple> executeQuery(String queryString, Datasource datasource){
        queryString = queryString.trim();
        log.info("Query will be executed: "+ queryString);
        Query query = getEntityManager(datasource).createNativeQuery(queryString, Tuple.class);
        List<Tuple> result = query.getResultList();
        log.info("results size: "+ result.size());
        return result;
    }

    public List<Map<String, Object>> executeQueryUsingJdbcTemplate(String queryString, Datasource datasource, String type){
        queryString = queryString.trim();
        if (queryString.endsWith(";")) queryString = queryString.substring(0, queryString.length()-1);
        List<Map<String, Object>> result = null;
        EntityManager entityManager =  getEntityManager(datasource);
        EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
        DataSource dataSource =  info.getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        if(type.toLowerCase().equals("select")) result = jdbcTemplate.queryForList(queryString);
        else if(type.toLowerCase().equals("update")) jdbcTemplate.update(queryString);
        return result;
    }
    @Transactional
    public void executeBatchInsertQueryUsingJdbcTemplate(String[] queryString, Datasource datasource){
        log.info("batch insert will be executed");
        log.info(Arrays.toString(queryString));
        EntityManager entityManager =  getEntityManager(datasource);
        EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
        DataSource dataSource =  info.getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.batchUpdate(queryString);
    }

    private EntityManager getEntityManager(){
        return this.getEntityManager(Datasource.PRIMARY);
    }

    public EntityManager getEntityManager(Datasource datasource){
        EntityManager entityManager = null;
        switch (datasource){
            case SECONDARY:
                entityManager = this.secondaryEntityManager;
                break;
            default:
                entityManager = this.primaryEntityManager;
                break;
        }

        return entityManager;
    }
}
