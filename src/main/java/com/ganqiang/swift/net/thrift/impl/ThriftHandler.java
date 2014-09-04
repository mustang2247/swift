package com.ganqiang.swift.net.thrift.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import com.ganqiang.swift.conf.LocalConfig.Instance;
import com.ganqiang.swift.conf.RemoteConfig;
import com.ganqiang.swift.conf.RemoteConfigHandler;
import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.net.http.HttpProxy;
import com.ganqiang.swift.net.http.HttpProxyHandler;
import com.ganqiang.swift.net.thrift.GlobalCommand;
import com.ganqiang.swift.net.thrift.GlobalConfig;
import com.ganqiang.swift.net.thrift.GlobalResponse;
import com.ganqiang.swift.net.thrift.Job;
import com.ganqiang.swift.net.thrift.JobCommand;
import com.ganqiang.swift.net.thrift.JobResponse;
import com.ganqiang.swift.net.thrift.PingResponse;
import com.ganqiang.swift.net.thrift.SwiftController;
import com.ganqiang.swift.net.zk.ZKContextHandler;
import com.ganqiang.swift.prep.DelayContextHandler;
import com.ganqiang.swift.prep.LifeCycleSettingHandler;
import com.ganqiang.swift.seed.PageSizeSettingHandler;
import com.ganqiang.swift.storage.disk.DiskContextHandler;
import com.ganqiang.swift.storage.index.LuceneContextHandler;
import com.ganqiang.swift.timer.JobController;
import com.ganqiang.swift.timer.JobScheduler;
import com.ganqiang.swift.util.CalculateUtil;
import com.ganqiang.swift.util.DateUtil;
import com.sun.management.OperatingSystemMXBean;

public class ThriftHandler implements SwiftController.Iface
{
  private static final Logger logger = Logger.getLogger(ThriftHandler.class);

  @Override
  public JobResponse allot(Job job) throws TException
  {
    try{
      logger.info("The swift server is ready to loading job [" + job.getId() + "] ...");
      Instance instance = convertInstance(job);
      initConstants(instance);
      JobScheduler.remoteRun(instance);
      return new JobResponse(job.getId(), "ok", "Job ["+job.getId()+"] is ready.");
    }catch(Exception e){
      return new JobResponse(job.getId(), "error", "Job ["+job.getId()+"] scheduling failure : " + e.getMessage());
    }
  }

  @Override
  public JobResponse sendJobCommand(String jobid, JobCommand command) throws TException
  {
    try{
      JobController controller = Constants.task_map.get(jobid);
      if (controller == null) {
        return new JobResponse(jobid, "error","Job ["+jobid+"] task is not exist. " );
      }
      switch(command){
        case PAUSE:
          controller.pause();
          break;
        case CONTINUE:
          controller.continues();
          break;
        case CANCEL:
          controller.destory(jobid);
          break;
      }
      return new JobResponse(jobid, "ok","Job ["+jobid+"] has been executed job command.");
    } catch (Exception e){
      return new JobResponse(jobid, "error","Job ["+jobid+"] scheduling failure : " + e.getMessage());
    }
  }

  @Override
  public GlobalConfig view() throws TException
  {
    logger.info("The swift server is ready to obtain configuration file.");
    return new RemoteConfigHandler().get();
  }

  @Override
  public GlobalResponse update(GlobalConfig globalconfig) throws TException
  {
    try{
      logger.info("The swift server is ready to update configuration file.");
      new RemoteConfigHandler().update(globalconfig);
      return new GlobalResponse("ok","Global configure updated successful.");
    } catch (Exception e){
      return new GlobalResponse("error","Global configure updated failure : " + e.getMessage());
    }
  }

  @Override
  public void sendGlobalCommand(GlobalCommand globalcommand)
      throws TException
  {
    switch(globalcommand){
      case STOP:
        logger.info("The swift server is ready to stop running.");
        break;
      case RESTART:
        logger.info("The swift server is ready to restart running.");
        break;
    }
    new GlobalCommandExecutor(globalcommand).execute();
  }
  
  @Override
  public PingResponse ping() throws TException {
		  PingResponse res = new PingResponse();
		  String osName = System.getProperty("os.name");
		  res.setOs(osName);
		  res.setCpurate(String.valueOf(getCpurate()));
		  res.setMemrate(String.valueOf(getMemRate()));
  		return res;
  	 }
  
  public static String getMemRate(){ 
		  OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean(); 
		  long total =  osmxb.getTotalPhysicalMemorySize() / 1024/1024;
		  long free = osmxb.getFreePhysicalMemorySize() / 1024/1024 ;
		  double d= CalculateUtil.div((total-free),total,4) * 100;
		  return String.valueOf(d);
	} 

  public static String getCpurate() {
     File file = new File("/proc/stat");
     BufferedReader br;
     Float result = null;
     try {
    		  br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    		  StringTokenizer token = new StringTokenizer(br.readLine());
		     token.nextToken();
		     int user1 = Integer.parseInt(token.nextToken());
		     int nice1 = Integer.parseInt(token.nextToken());
		     int sys1 = Integer.parseInt(token.nextToken());
		     int idle1 = Integer.parseInt(token.nextToken());
		     Thread.sleep(1000);
		     br = new BufferedReader(
		     new InputStreamReader(new FileInputStream(file)));
		     token = new StringTokenizer(br.readLine());
		     token.nextToken();
		     int user2 = Integer.parseInt(token.nextToken());
		     int nice2 = Integer.parseInt(token.nextToken());
		     int sys2 = Integer.parseInt(token.nextToken());
		     int idle2 = Integer.parseInt(token.nextToken());
		     result = (float)((user2 + sys2 + nice2) - (user1 + sys1 + nice1)) / (float)((user2 + nice2 + sys2 + idle2) - (user1 + nice1 + sys1 + idle1));
     } catch (Exception e) {
    		  e.printStackTrace();
             }
     result = result * 100;
     return String.format("%.2f", result);
     }
  

  private void initConstants(Instance instance){
    Constants.inside_use_proxy_map.put(instance.getId(), instance.isInUseProxy());
    Constants.outside_use_proxy_map.put(instance.getId(), instance.isOutUseProxy());
    Constants.js_support_map.put(instance.getId(), instance.isJssupport());
    Constants.sync_map.put(instance.getId(), instance.isSync());
    Constants.sync_domain_map.put(instance.getId(), instance.getSyncDomain());
    Constants.disk_path_map.put(instance.getId(), instance.getDisk());
    Constants.inside_delay_map.put(instance.getId(), instance.getIndelay());
    Constants.outside_delay_map.put(instance.getId(), instance.getOutdelay());
    HttpProxyHandler httpproxy = new HttpProxyHandler();
    PageSizeSettingHandler pagesize = new PageSizeSettingHandler();
    DiskContextHandler disk = new DiskContextHandler();
    LuceneContextHandler lucene = new LuceneContextHandler();
    DelayContextHandler delay = new DelayContextHandler();
    LifeCycleSettingHandler lifecycle = new LifeCycleSettingHandler();
    logger.info("checking remote http proxy...");
    httpproxy.remoteCheck(instance);
    logger.info("checking remote http proxy finish.");
    logger.info("setting remote inside seed page size...");
    pagesize.init(instance);
    logger.info("setting remote inside seed page size finish.");
    logger.info("setting remote disk context...");
    disk.init(instance);
    logger.info("setting remote disk finish.");
    logger.info("setting remote lucene context...");
    lucene.init(instance);
    logger.info("setting remote lucene finish.");
    logger.info("setting remote delay context...");
    delay.init(instance);
    logger.info("setting remote delay finish.");
    logger.info("setting remote lifecycle context...");
    lifecycle.setting(instance);
    logger.info("setting remote lifecycle finish.");
  }

  private Instance convertInstance(Job job){
    RemoteConfig resource = Constants.remote_config;
    Instance instance = new Instance();
    instance.setId(job.getId());
    instance.setIndelay(job.getInDelay());
    instance.setOutdelay(job.getOutDelay());
    instance.setCascade(job.isCascade);   
    instance.setDepth(job.getDepth());
    instance.setHttpProxys(convertHttpProxy(job.getHttpProxys()));
    instance.setInIsdownload(job.inIsdownload);
    instance.setInSeeds(convertArray(job.getInSeeds()));
    instance.setInterval(job.getInterval());
    instance.setInUseProxy(job.inUseProxy);
    instance.setJssupport(job.jsSupport);
    instance.setOutIsdownload(job.outIsdownload);
    instance.setOutSeeds(convertArray(job.outSeeds));
    instance.setOutUseProxy(job.outUseProxy);
    instance.setStartTime(DateUtil.parse(job.getStartTime()));
    instance.setDbDriver(resource.getDbDriver());
    instance.setDbPassword(resource.getDbPassword());
    instance.setDbPoolSize(resource.getDbPoolSize());
    instance.setDbUrl(resource.getDbUrl());
    instance.setDbUsername(resource.getDbUsername());
    instance.setDisk(resource.getDisk());
    instance.setIndex(resource.getIndex());
    instance.setSync(resource.isSync());
    instance.setSyncDomain(resource.getSyncDomain());
    instance.setAddress(resource.getAddress());
    instance.setTotalNodes(resource.getTotalNodes());
    instance.setSeqId(resource.getSeqId());
    return instance;
  }
  
  private List<HttpProxy> convertHttpProxy(List<String> list){
    if (list == null || list.size() == 0) {
      return null;
    }
    List<HttpProxy> nproxys = new ArrayList<HttpProxy>();
    for (int i=0; i< list.size(); i++) {
      String proxy = list.get(i).trim();
      HttpProxy httpProxy = null;
      if (proxy.contains(",")) {
        String[] userpwdproxy = proxy.split("\\,");
        String hp = userpwdproxy[0].trim();
        String up = userpwdproxy[1].trim();
        String[] hppart = hp.split("\\:");
        String[] uppart = up.split("\\:");
        HttpHost host = new HttpHost(hppart[0].trim(), Integer.valueOf(hppart[1].trim()));
        httpProxy = new HttpProxy(host, uppart[0].trim(), uppart[1].trim());
      } else {
        String[] hppart = proxy.split("\\:");
        HttpHost host = new HttpHost(hppart[0].trim(), Integer.valueOf(hppart[1].trim()));
        httpProxy = new HttpProxy(host);
      }
      nproxys.add(httpProxy);
    }
    return nproxys;
  }

  private String[] convertArray(List<String> list){
    if (list == null || list.size() == 0) {
      return null;
    }
    String[] array = new String[list.size()];
    for (int i=0;i<list.size(); i++) {
      String str = list.get(i);
      array[i] = str;
    }
    return array;
  }



}
