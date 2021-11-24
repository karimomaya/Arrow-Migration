package com.asset.migration.parser;

import com.asset.migration.config.IMigrationConfig;
import com.asset.migration.util.IJsonAdapter;

import java.io.File;

public interface IParser {
    public IParser setJsonAdaptee(IJsonAdapter jsonAdapter);
    public IParser setMigrationConfig(IMigrationConfig migrationConfig);
    public IMigrationConfig parse(File file);

}
