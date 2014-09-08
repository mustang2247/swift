package com.ganqiang.swift.storage.hbase;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.core.Process;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.storage.db.DBStore;
import com.ganqiang.swift.storage.db.SqlBuilder;

public class HBaseStorage implements Process{

    private static final Logger logger = Logger.getLogger(HBaseStorage.class);
    
    private static final HBaseStore store = new HBaseStore();

    @Override
    public void execute(Event event) {
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- hbase begin write detail page data.");
        Seed seed = (Seed) event.get(Event.seed_key);
        SiteType type = seed.getType();
        String instanceid = seed.getId();
        List<Result> results = (List<Result>) event.get(Event.results_key);
        HConnectionController hcc =  Constants.local_hbase_map.get(instanceid);
        if (hcc == null) {
            hcc = Constants.remote_hbase;
        }
        List<Result> newresults = new ArrayList<Result>();

        for(Result result : results){
            Result dbresult = store.readOne(hcc, result.getUrl());
            if(dbresult == null || result.isRequireUpdate(dbresult)){
                newresults.add(result);
             }
        }
        store.writeBatch(hcc, newresults);
        logger.info("Worker [" + Thread.currentThread().getName() + "] --- hbase  end write detail page data.");
        
    }

}
