package com.ganqiang.swift.storage.db;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.core.Process;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.storage.Result;

public class DBStorage implements Process {

	private static final Logger logger = Logger.getLogger(DBStorage.class);

	@SuppressWarnings("unchecked")
	@Override
	public void execute(Event event) {
		logger.info("Worker [" + Thread.currentThread().getName()
				+ "] --- db  begin write detail page data.");
		Seed seed = (Seed) event.get(Event.seed_key);
		SiteType type = seed.getType();
		String instanceid = seed.getId();
		List<Result> results = (List<Result>) event.get(Event.results_key);
		DataSource ds = Constants.local_datasource_map.get(instanceid);
		if (ds == null) {
			ds = Constants.remote_datasource;
		}
		DBStore dbstore = new DBStore(ds);
		SqlBuilder sqlbuilder = new SqlBuilder();
		sqlbuilder.buildSqls(dbstore, results, type, instanceid);
		List<String> insertsqls = sqlbuilder.getInsertSqls();
		List<String> updatesqls = sqlbuilder.getUpdateSqls();
		if (insertsqls.size() > 0) {
			dbstore.writeBatch(insertsqls);
		}
		if (updatesqls.size() > 0) {
			dbstore.writeBatch(updatesqls);
		}
		List<String> insertdaysqls = sqlbuilder.getInsertDaySqls();
		if (insertdaysqls.size() > 0) {
			dbstore.writeBatch(insertdaysqls);
		}
		logger.info("Worker [" + Thread.currentThread().getName()
				+ "] --- db  end write detail page data.");
	}

}
