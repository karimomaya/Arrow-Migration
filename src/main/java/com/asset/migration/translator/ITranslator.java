package com.asset.migration.translator;

import com.asset.migration.config.IMigrationConfig;
import com.asset.migration.query.IMigrationQuery;

import java.util.List;

public interface ITranslator {
    public List<IMigrationQuery> translate(IMigrationConfig migrationQuery);
    public List<IMigrationQuery> translate(List<IMigrationQuery> migrationQueries);
}
