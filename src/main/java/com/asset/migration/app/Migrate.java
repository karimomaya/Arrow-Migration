package com.asset.migration.app;

import com.asset.migration.execution.IQueryExecutable;
import com.asset.migration.util.IJsonAdapter;
import com.asset.migration.parser.IParser;
import com.asset.migration.config.IMigrationConfig;
import com.asset.migration.query.IMigrationQuery;
import com.asset.migration.translator.ITranslator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

@Service
@Data
@Slf4j
public class Migrate {
    @Autowired
    @Qualifier("primaryEntityManager")
    private EntityManager primaryEntityManager;
    @Autowired
    @Qualifier("secondEntityManager")
    private EntityManager secondaryEntityManager;

    private File file;
    @Value( "${app.config}" )
    private String CONFIGPATH;
    private IParser parser;
    private IJsonAdapter jsonAdapter;
    private IMigrationConfig migrationConfig;
    private ITranslator translator;
    private IQueryExecutable queryExecutable;

    public Migrate(){}

    public Migrate setParser(IParser parser){
        this.parser= parser;
        return this;
    }

    public Migrate setJsonAdapter(IJsonAdapter jsonAdapter){
        this.jsonAdapter= jsonAdapter;
        return this;
    }

    public Migrate setMigrationConfig(IMigrationConfig migrationConfig){
        this.migrationConfig= migrationConfig;
        return this;
    }

    public Migrate setTranslator(ITranslator translator){
        this.translator= translator;
        return this;
    }

    public Migrate setQueryExecutable(IQueryExecutable queryExecutable){
        this.queryExecutable= queryExecutable;
        return this;
    }
    public void run(){
        if (parser == null || jsonAdapter == null || migrationConfig == null || translator == null || queryExecutable == null){
            log.error("You cannot run query without initialize IParser, IJsonAdapter, IMigrationConfig, ITranslator and  IQueryExecutable");
            throw new RuntimeException("You cannot run query without initialize IParser, IJsonAdapter, IMigrationConfig, ITranslator and  IQueryExecutable");

        }

        run(parser, jsonAdapter, migrationConfig, translator, queryExecutable);

    }

    public void run(IParser parser, IJsonAdapter jsonAdapter, IMigrationConfig migrationConfig, ITranslator translator, IQueryExecutable queryExecutable){
        try {
            file = ResourceUtils.getFile(CONFIGPATH);
            parser = parser.setJsonAdaptee(jsonAdapter).setMigrationConfig(migrationConfig);
            migrationConfig = parser.parse(file);
            List<IMigrationQuery> migrationQueries = translator.translate(migrationConfig);
            migrationQueries =queryExecutable.executeQueries(migrationQueries);
            translator.translate(migrationQueries);
            queryExecutable.executeToQueries(migrationQueries);
            log.info("********Congrats Migration Complete*********");
        }catch (FileNotFoundException e) {
            log.error("File Not Found: "+ e.getMessage());
            e.printStackTrace();
        }
    }

}
