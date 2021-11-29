package com.asset.migration.execution;

import com.asset.migration.enums.Datasource;
import com.asset.migration.query.IMigrationQuery;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import java.util.Arrays;
import java.util.List;

@Data
public class QueryExecutable implements IQueryExecutable {
    private EntityManager primaryEntityManager;
    private EntityManager secondaryEntityManager;
    private IMigrationQuery migrationQuery;

    public QueryExecutable(){}
    public QueryExecutable(EntityManager primaryEntityManager, EntityManager secondaryEntityManager){
        this.primaryEntityManager = primaryEntityManager;
        this.secondaryEntityManager = secondaryEntityManager;
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

    @Transactional
    public void executeToQueries(List<IMigrationQuery> migrationQueries){
        migrationQueries.stream().forEach(migrationQuery->{
            String[] result = migrationQuery.getToQuery().split("\\r?\\n");
            Arrays.stream(result).forEach((query)->{
                executeQuery(query, Datasource.SECONDARY);
            });
//Object relation mapping - query db  - sql injection - validate - trim - transaction - connection retry to connect
        });
    }

    public IMigrationQuery executeQuery(IMigrationQuery migrationQuery){
        List<Tuple> result = executeQuery(migrationQuery.getSelectQuery(), Datasource.PRIMARY);
        migrationQuery.setResult(result);
        return migrationQuery;
    }

    @Transactional
    public List<Tuple> executeQuery(String queryString, Datasource datasource){
        Query query = getEntityManager(datasource).createNativeQuery(queryString, Tuple.class);

        List<Tuple> result = query.getResultList();

        return result;
    }

    private EntityManager getEntityManager(){
        return this.getEntityManager(Datasource.PRIMARY);
    }

    @Transactional
    public EntityManager getEntityManager(Datasource datasource){
        EntityManager entityManager = null;
        switch (datasource){
            case PRIMARY:
                entityManager = this.primaryEntityManager;
            case SECONDARY:
                entityManager = this.secondaryEntityManager;
            default:
                entityManager = this.primaryEntityManager;
        }

        return entityManager;
    }
}
