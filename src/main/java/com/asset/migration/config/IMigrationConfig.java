package com.asset.migration.config;

import java.util.List;

public interface IMigrationConfig {
    public List<MigrationConfig.TableConfig> getTables();
}
