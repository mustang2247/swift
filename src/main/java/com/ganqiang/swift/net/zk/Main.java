package com.ganqiang.swift.net.zk;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

public class Main {

	public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
		// TODO Auto-generated method stub
		ZooKeeper zk = new ZooKeeper("localhost:2181", 10000, null);
//		zk.create("/queue", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		zk.create("/queue/1", null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		zk.create("/queue/start", null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		TimeUnit.SECONDS.sleep(30000);
	}

}
