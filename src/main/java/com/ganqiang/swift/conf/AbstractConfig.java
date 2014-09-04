package com.ganqiang.swift.conf;

import org.apache.log4j.Logger;
import org.dom4j.Document;

import com.ganqiang.swift.util.StringUtil;

public abstract class AbstractConfig implements Config
{
  private static final Logger logger = Logger.getLogger(AbstractConfig.class);
  protected static final String default_disk_path = System.getProperty("user.dir")+"/data/";
  protected static final String default_index_path = System.getProperty("user.dir")+"/data/index/";
  /******** xml node ************/
  protected static final String swift_node = "swift";
  protected static final String thread_num_node = "thread_num";
  protected static final String instance_node = "instance";
  protected static final String id_node = "id";
  protected static final String timer_node = "timer";
  protected static final String start_time_node = "start_time";
  protected static final String interval_node = "interval";
  protected static final String http_proxy_node = "http_proxy";
  protected static final String seeds_node = "seeds";
  protected static final String inside_node = "inside";
  protected static final String outside_node = "outside";
  protected static final String is_download_node = "is_download";
  protected static final String js_support_node = "js_support";
  protected static final String delay_node = "delay";
  protected static final String use_proxy_node = "use_proxy";
  protected static final String is_cascade_node = "is_cascade";
  protected static final String depth_node = "depth";
  protected static final String storage_node = "storage";
  protected static final String is_sync_node = "is_sync";  
  protected static final String sync_domain_node = "sync_domain";
  protected static final String db_node = "db";
  protected static final String thrift_server_node = "thrift_server";
  protected static final String driver_node = "driver";
  protected static final String url_node = "url";
  protected static final String user_name_node = "user_name";  
  protected static final String password_node = "password";  
  protected static final String pool_size_node = "pool_size";
  protected static final String disk_node = "disk";  
  protected static final String index_node = "index";  
  protected static final String thrift_node = "thrift";  
  protected static final String port_node = "port";  
  protected static final String server_node = "server";
  protected static final String protocal_node = "protocal";
  protected static final String zookeeper_node = "zookeeper";  
  protected static final String seq_id_node = "seq_id";
  protected static final String address_node = "address";
  protected static final String total_nodes_node = "total_nodes";
  /******** xml node value ************/
  public static final String simple_server_value = "simple";
  public static final String threadpool_server_value = "threadpool";
  public static final String hsha_server_value = "hsha";
  public static final String nonblocking_server_value = "nonblocking";
  public static final String compact_protocal_value = "compact";
  public static final String binary_protocal_value = "binary";
  public static final String json_protocal_value = "json";

  public void validate(){
    String validateFile = getValidateFile();
    if (StringUtil.isNullOrBlank(validateFile)) {
      return;
    }
    boolean flag = XmlHelper.Validate(getConfigFile(), validateFile);
    if(!flag){
      logger.error("XML file verification failed.");
      System.exit(1);
    }else{
      logger.info("Xml file has been successfully verified.");
    }
  }
  
  protected Document getDocument(){
    Document doc = XmlHelper.loadXML(getConfigFile());
    return doc;
  }
  
//  protected Element getRoot(){
//    Document doc = getDocument();
//    return doc.getRootElement();
//  }
//  
  
  abstract String getConfigFile();
  
  abstract String getValidateFile();

}
