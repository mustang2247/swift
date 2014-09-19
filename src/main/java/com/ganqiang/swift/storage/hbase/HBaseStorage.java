package com.ganqiang.swift.storage.hbase;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.core.Process;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.storage.Result;

public class HBaseStorage implements Process {

	private static final Logger logger = Logger.getLogger(HBaseStorage.class);

	private static final HBaseStore store = new HBaseStore();

	@SuppressWarnings("unchecked")
	@Override
	public void execute(Event event) {
		logger.info("Worker [" + Thread.currentThread().getName()
				+ "] --- hbase begin write detail page data.");
		Seed seed = (Seed) event.get(Event.seed_key);
//		SiteType type = seed.getType();
		String instanceid = seed.getId();
		List<Result> results = (List<Result>) event.get(Event.results_key);
		HConnectionController hcc = Constants.local_hbase_map.get(instanceid);
		if (hcc == null) {
			hcc = Constants.remote_hbase;
		}
		List<Result> newresults = new ArrayList<Result>();

		for (Result result : results) {
			Result dbresult = store.readOne(hcc, result.getUrl());
			if (dbresult == null || result.isRequireUpdate(dbresult)) {
				newresults.add(result);
			}
		}
		store.writeBatch(hcc, newresults);

		logger.info("Worker [" + Thread.currentThread().getName()
				+ "] --- hbase  end write detail page data.");

	}

}
