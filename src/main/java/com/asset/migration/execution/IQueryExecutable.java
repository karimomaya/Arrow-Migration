package com.asset.migration.execution;

import com.asset.migration.enums.Datasource;
import com.asset.migration.query.IMigrationQuery;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.util.List;

public interface IQueryExecutable {
    public void setPrimaryEntityManager(EntityManager primaryEntityManager);
    public void setSecondaryEntityManager(EntityManager secondaryEntityManager);
    public void setMigrationQuery(IMigrationQuery migrationQuery);
    public IMigrationQuery executeQuery();
    public List<Tuple> executeQuery(String query, Datasource datasource);
    public IMigrationQuery executeQuery(IMigrationQuery migrationQuery);
    public List<IMigrationQuery> executeQueries(List<IMigrationQuery> migrationQueries);
    public void  executeToQueries(List<IMigrationQuery> migrationQueries);
}
