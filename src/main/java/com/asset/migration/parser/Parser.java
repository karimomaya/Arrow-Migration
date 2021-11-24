package com.asset.migration.parser;

import com.asset.migration.config.IMigrationConfig;
import com.asset.migration.util.IJsonAdapter;
import lombok.Data;

import java.io.File;
import java.io.IOException;

@Data
public class Parser implements IParser{

    IJsonAdapter jsonAdaptee;
    IMigrationConfig migrationConfig;

    public Parser(IJsonAdapter jsonAdaptee, IMigrationConfig migrationConfig){
        this.jsonAdaptee = jsonAdaptee;
        this.migrationConfig = migrationConfig;
    }

    public Parser(){}

    @Override
    public IParser setJsonAdaptee(IJsonAdapter jsonAdapter){
        this.jsonAdaptee = jsonAdapter;
        return this;
    }


    public IParser setMigrationConfig(IMigrationConfig migrationConfig){
        this.migrationConfig = migrationConfig;
        return this;
    }

    @Override
    public IMigrationConfig parse(File file){
        if (migrationConfig == null) throw new RuntimeException("You need to initialize MigrationConfig class");

        IMigrationConfig result = null;
        try {
            result = this.jsonAdaptee.convert(file, migrationConfig.getClass());
            System.out.println(result.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


}
