package com.ganqiang.swift.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.tomcat.jdbc.pool.DataSource;

import com.ganqiang.swift.conf.LocalConfig;
import com.ganqiang.swift.conf.RemoteConfig;
import com.ganqiang.swift.conf.SyncConfig;
import com.ganqiang.swift.net.http.HttpProxy;
import com.ganqiang.swift.net.http.HttpProxyLooper;
import com.ganqiang.swift.seed.InsideSeed;
import com.ganqiang.swift.seed.Seed.SiteType;
import com.ganqiang.swift.storage.hbase.HConnectionController;
import com.ganqiang.swift.timer.JobController;

public final class Constants
{
  //key: instanceid or jobid value: JobController
  public static HashMap<String, JobController> task_map = new HashMap<String, JobController>();

  //process---------------------------------------------------------------------
  //key: instanceid or jobid value: Chain
  public static Map<String, Chain> chain_map = new HashMap<String, Chain>();

  //init---------------------------------------------------------------------
  public static CloseableHttpClient http_client = null;
  public static RequestConfig http_request_config = null;
  //key: instanceid + sitetype (or site) value: http_proxys
  public static ConcurrentHashMap<String, HttpProxyLooper> proxy_map = new ConcurrentHashMap<String, HttpProxyLooper>();
  //key: instanceid + sitetype (or site) value: http_proxys
  public static HashMap<String, HttpClientContext> http_context_map = new HashMap<String, HttpClientContext>();

  public static void addHttpProxy(String key, HttpProxy proxy){
    HttpProxyLooper useproxy = proxy_map.get(key);
    if(useproxy == null){
      useproxy = new HttpProxyLooper();
      useproxy.addQueue(proxy);
      proxy_map.put(key, useproxy);
    }
  }

  // key : instanceid  value: datasource  单机版可以有多个instance的db配置
  public static HashMap<String, DataSource> local_datasource_map = new HashMap<String, DataSource>();
  // key : instanceid  value: thrift_server
  public static HashMap<String, String> local_ts_map = new HashMap<String, String>();
  // key : instanceid  value: hbase_config
  public static HashMap<String, HConnectionController> local_hbase_map = new HashMap<String, HConnectionController>();

  //分布式版只能有一个instance的db配置
  public static DataSource remote_datasource = new DataSource();
  public static String remote_ts = null;
  public static HConnectionController remote_hbase = new HConnectionController();

  //config---------------------------------------------------------------------
  public static LocalConfig local_config = null;
  public static RemoteConfig remote_config = null;
  public static SyncConfig sync_config = null;
  public static Integer thread_num = null;
  //key: instanceid value: jssupport
  public static HashMap<String, Boolean> js_support_map = new HashMap<String, Boolean>();  
  //key: instanceid value: true/false
  public static HashMap<String, Boolean> inside_use_proxy_map = new HashMap<String, Boolean>();
  //key: instanceid value: true/false
  public static HashMap<String, Boolean> outside_use_proxy_map = new HashMap<String, Boolean>();
  //key: instanceid value: disk_path
  public static HashMap<String, String> disk_path_map = new HashMap<String, String>();
  //key: instanceid value: is_sync
  public static HashMap<String, Boolean> sync_map = new HashMap<String, Boolean>();
  //key: instanceid value: sync_domain
  public static HashMap<String, String> sync_domain_map = new HashMap<String, String>();    
  //key: instanceid value: index_path
  public static HashMap<String, String> inside_index_path_map = new HashMap<String, String>();
  //key: instanceid+site value: site avatar path
  public static HashMap<String, String> inside_avatar_path_map = new HashMap<String, String>();  
  //key: instanceid+site value: site html path
  public static HashMap<String, String> inside_page_path_map = new HashMap<String, String>();
  //key: instanceid+site value: site html path
  public static HashMap<String, String> outside_page_path_map = new HashMap<String, String>();  
  //key: instanceid value: delay
  public static HashMap<String, Long> inside_delay_map = new HashMap<String, Long>();
  //key: instanceid value: delay
  public static HashMap<String, Long> outside_delay_map = new HashMap<String, Long>();
  //key： instanceid value: depth
  public static HashMap<String, Integer> depth_map = new HashMap<String, Integer>();

  //site delay---------------------------------------------------------------------
  //key: instanceid + site   value: timestamp
  public static HashMap<String, Long> all_delay_map = new HashMap<String, Long>();

  //site cache---------------------------------------------------------------------
  public static final String split_str = "|";
//  public static Map<Enum<SiteType>, Page> all_page_map = new HashMap<Enum<SiteType>, Page>();

  //timer---------------------------------------------------------------------
  //key: instanceid  value: scheduleTask 
//  public static HashMap<String, ScheduleTask> taskmap = new HashMap<String, ScheduleTask>();
  public static Timer timer = null;

  //result---------------------------------------------------------------------
  public static final String status_ymb = "已满标";
  public static final String status_yjk = "已借款";
  public static final String status_yhw = "已还完";
  public static final String status_ylb = "已流标";
  public static final String status_yzr = "已转让";
  public static final String status_ysw = "已售完";
  public static final String status_hkz = "还款中";
  public static final String status_tbz = "投标中";
  public static final String status_jpz = "竞拍中";
  public static final String status_dqr = "待确认";
  public static final String status_dsh = "待审核";
  public static final String zero_remain_time = "0天0小时0分钟";
  public static final Double zero_remain_money = 0d;//单位:元

  //page---------------------------------------------------------------------
  public static HashMap<SiteType, InsideSeed> seed_map = new HashMap<SiteType, InsideSeed>();

  //http---------------------------------------------------------------------
  public static final String listpage_filename = "list-page-";
  public static final String detailpage_filename = "detail-page-";

  //key： instanceid value: seq_id
  public static HashMap<String, Integer> local_seq_id_map = new HashMap<String, Integer>();
  //key： instanceid value: address
  public static HashMap<String, String> local_address_map = new HashMap<String, String>();
  //key： instanceid value: total_nodes
  public static HashMap<String, Integer> local_total_nodes_map = new HashMap<String, Integer>();
 
  public static Integer remote_seq_id = null;
  public static String remote_address = null;
  public static Integer remote_total_nodes = null;
 
  public static final String hbase_table_name = "p2p";
  public static final String hbase_column_family = "cf";
}
