package com.ganqiang.swift;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.ganqiang.swift.net.thrift.impl.ThriftServer;
import com.ganqiang.swift.prep.Prepare;
import com.ganqiang.swift.prep.StartupMode;
import com.ganqiang.swift.prep.Visitor;
import com.ganqiang.swift.timer.JobScheduler;

public class Swift
{
  private static final Logger logger = Logger.getLogger(Swift.class);
  private static final String log4j = System.getProperty("user.dir")+"/conf/log4j.conf";

  static{
    PropertyConfigurator.configure(log4j);
    logger.info("loading log4j.conf file...");
  }

  private void startup(StartupMode mode){
    if (mode.equals(StartupMode.local)) {
      Visitor visitor = new Prepare(StartupMode.local);
      visitor.visitAll();
      JobScheduler.localRun();
    } else {
      Visitor visitor = new Prepare(StartupMode.remote);
      visitor.visitAll();
      ThriftServer.start();
    }
  }

  public static void main(String... args){
    if (args == null) {
      logger.error("Boot parameters not specified.");
      System.exit(1);
    }

    if (args[0].equalsIgnoreCase("local")) {
      logger.info("Start the local mode......");
      Swift swift = new Swift();
      swift.startup(StartupMode.local);
    }else if(args[0].equalsIgnoreCase("remote")){
      logger.info("Start the remote mode......");
      Swift swift = new Swift();
      swift.startup(StartupMode.remote);
    }
  }

}
