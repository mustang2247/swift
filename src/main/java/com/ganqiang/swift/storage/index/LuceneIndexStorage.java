package com.ganqiang.swift.storage.index;

import java.util.List;

import org.apache.log4j.Logger;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.core.Process;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.storage.Storable;

public class LuceneIndexStorage implements Process
{
  private static final Logger logger = Logger.getLogger(LuceneIndexStorage.class);

  @SuppressWarnings("unchecked")
  @Override
  public void execute(Event event)
  {
    Seed seed = (Seed) event.get(Event.seed_key);
    String key = seed.getKey();
    String url = seed.getListUrl();
    String seedname = seed.getSeedName();
    logger.info("Worker [" + Thread.currentThread().getName() + "] begin index ["+ url +"] data.");
    List<Result> results = (List<Result>) event.get(Event.results_key);
    String siteindex = Constants.inside_index_path_map.get(key);
//  for (Result result : results) {
//  if (!type.equals(SiteType.OUTSIDE)) {
//    LuceneIndexStore lis = new LuceneIndexStore(seedname, siteindex);
////    Result searchResult = dao.searchOne(site, siteindex, result);
////    if(result.isRequireLuceneUpdate(searchResult)){
//      dao.write(site, siteindex, result);
////    }
//  }
//}
    Storable lis = new LuceneIndexStore(seedname, siteindex);
    lis.writeBatch(results);
    logger.info("Worker [" + Thread.currentThread().getName() + "] end index ["+ url +"] data.");
  }

}
