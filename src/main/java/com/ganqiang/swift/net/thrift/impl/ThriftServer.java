package com.ganqiang.swift.net.thrift.impl;

import org.apache.log4j.Logger;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;

import com.ganqiang.swift.conf.RemoteConfig;
import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.net.thrift.SwiftController;
import com.ganqiang.swift.util.StringUtil;

public final class ThriftServer
{
  private static final Logger logger = Logger.getLogger(ThriftServer.class);
  private static int server_port;
  private static TProtocolFactory tProtocalFactory;

  public static void start(){
    RemoteConfig resource = Constants.remote_config;
    Integer serverPort = resource.getPort();
    if(serverPort == null){
      server_port = 5678;
    } else {
      server_port = serverPort;
    }
    String server = resource.getServer();
    String protocal = resource.getProtocal();
    if(StringUtil.isNullOrBlank(protocal)){
      protocal = "binary";
    } else {
      if (protocal.equalsIgnoreCase("binary")) {
        tProtocalFactory = new TBinaryProtocol.Factory();
      } else if(protocal.equalsIgnoreCase("compact")){
        tProtocalFactory = new TCompactProtocol.Factory();
      } else if(protocal.equalsIgnoreCase("json")){
        tProtocalFactory = new TJSONProtocol.Factory();
      }
    }
    if ("simple".equalsIgnoreCase(server)) {
      startSimpleServer();
    } else if("threadpool".equalsIgnoreCase(server)){
      startThreadPoolServer();
    } else if("hsha".equalsIgnoreCase(server)){
      startHsHaServer();
    } else if("nonblocking".equalsIgnoreCase(server)){
      startNonblockingServer();
    }
  }

  //使用非阻塞式IO，服务端和客户端需要指定TFramedTransport数据传输的方式
  private static void startNonblockingServer()
  {
    try {
      logger.info("Thrift Nonblocking Server start ....");
      TProcessor tprocessor = new SwiftController.Processor<SwiftController.Iface>(new ThriftHandler());
      TNonblockingServerSocket tnbSocketTransport = new TNonblockingServerSocket(server_port);
      TNonblockingServer.Args tnbArgs = new TNonblockingServer.Args(tnbSocketTransport);
      tnbArgs.processor(tprocessor);
      tnbArgs.transportFactory(new TFramedTransport.Factory());
      tnbArgs.protocolFactory(tProtocalFactory);
      TServer server = new TNonblockingServer(tnbArgs);
      server.serve();
    } catch (Exception e) {
      logger.error("Thrift Nonblocking Server start error!!!");
    }
  }

  private static void startSimpleServer()
  {
    try {
      logger.info("Thrift Simple Server start ....");
      TProcessor tprocessor = new SwiftController.Processor<SwiftController.Iface>(new ThriftHandler());
      TServerSocket serverTransport = new TServerSocket(server_port);
      TServer.Args tArgs = new TServer.Args(serverTransport);
      tArgs.processor(tprocessor);
      tArgs.protocolFactory(new TBinaryProtocol.Factory());
      // tArgs.protocolFactory(new TCompactProtocol.Factory());
      // tArgs.protocolFactory(new TJSONProtocol.Factory());
      TServer server = new TSimpleServer(tArgs);
      server.serve();
      
    } catch (Exception e) {
      logger.error("Thrift Simple Server start error!!!");
    }
  }

  //线程池服务模型，使用标准的阻塞式IO，预先创建一组线程处理请求。
  private static void startThreadPoolServer()
  {
    try {
      logger.info("Thrift ThreadPool Server start ....");
      TProcessor tprocessor = new SwiftController.Processor<SwiftController.Iface>(new ThriftHandler());
      TServerSocket serverTransport = new TServerSocket(server_port);
      TThreadPoolServer.Args ttpsArgs = new TThreadPoolServer.Args(serverTransport);
      ttpsArgs.processor(tprocessor);
      ttpsArgs.protocolFactory(new TBinaryProtocol.Factory());
      // ttpsArgs.protocolFactory(new TCompactProtocol.Factory());
      // ttpsArgs.protocolFactory(new TJSONProtocol.Factory());
      TServer server = new TThreadPoolServer(ttpsArgs);
      server.serve();
    } catch (Exception e) {
      logger.error("Thrift ThreadPool Server start error!!!");
    }
  }

  //半同步半异步的服务模型
  private static void startHsHaServer()
  {
    try {
      logger.info("Thrift HsHa Server start ....");
      TProcessor tprocessor = new SwiftController.Processor<SwiftController.Iface>(new ThriftHandler());
      TNonblockingServerSocket tnbSocketTransport = new TNonblockingServerSocket(server_port);
      THsHaServer.Args thhsArgs = new THsHaServer.Args(tnbSocketTransport);
      thhsArgs.processor(tprocessor);
      thhsArgs.transportFactory(new TFramedTransport.Factory());
      thhsArgs.protocolFactory(new TBinaryProtocol.Factory());
      TServer server = new THsHaServer(thhsArgs);
      server.serve();
    } catch (Exception e) {
      logger.error("Thrift HsHa Server start error!!!");
    }
  }

}
