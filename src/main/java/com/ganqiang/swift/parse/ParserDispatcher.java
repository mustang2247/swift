package com.ganqiang.swift.parse;

import java.util.List;

import org.apache.log4j.Logger;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.core.Process;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.storage.Result;

public final class ParserDispatcher implements Process
{

  private static final Logger logger = Logger.getLogger(ParserDispatcher.class);

  @Override
  public void execute(Event event)
  {
    logger.info("Worker [" + Thread.currentThread().getName() + "] begin ParserAdaptor.");
    Seed seed = (Seed) event.get(Event.seed_key);
    SiteType type = seed.getType();
    Parsable parser = Constants.seed_map.get(type).getParser();
    List<Result> results = parser.parse(event);
    event.put(Event.results_key, results);
    logger.info("Worker [" + Thread.currentThread().getName() + "] end ParserAdaptor.");
  }

}
