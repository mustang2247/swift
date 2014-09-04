package com.ganqiang.swift.net.zk;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;

public class ZKConnectionManager implements Watcher {

		private static final Logger logger = Logger.getLogger(ZKConnectionManager.class);

		public static final int SESSION_TIMEOUT = 30000;
		private static ZooKeeper zk;
		private CountDownLatch conSignal = new CountDownLatch(1);
		public static final int RETRY_MAX_COUNT = 5;

		public ZooKeeper getConnection(String host) {
				int retry = 0;
				try {
						zk = new ZooKeeper(host, SESSION_TIMEOUT, this);
						if (States.CONNECTING == zk.getState()) {  
	            try {
	            		conSignal.await();  
	            } catch (InterruptedException e) {  
	               throw new IllegalStateException(e);  
	            					}  
				    } else {
							  while (retry++ < RETRY_MAX_COUNT) {
									 logger.warn("tring to connect "+host+", retry count is " + retry);
									 reConnection(host);
								 }
							  if(retry == RETRY_MAX_COUNT){
								   logger.error(host+" is not connected.");
							     }
						}
				} catch (Exception e) {
						while (retry++ < RETRY_MAX_COUNT) {
								logger.warn("tring to connect, retry count is " + retry, e);
								reConnection(host);
						}
						if(retry == RETRY_MAX_COUNT){
						  logger.error(host+" is not connected.", e);
						}
				}
				return zk;
		}

		public void reConnection(String host){
				try {
						zk = new ZooKeeper(host, SESSION_TIMEOUT, this);
				} catch (Exception ex) {
						logger.error("retry connection is fault.", ex);
				}
		}

		@Override
		public void process(WatchedEvent event) {
			if (event.getState() == KeeperState.SyncConnected) {
				conSignal.countDown();
			}
		}
}
