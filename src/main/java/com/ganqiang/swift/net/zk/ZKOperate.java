package com.ganqiang.swift.net.zk;

import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public interface ZKOperate {
	
	ZooKeeper getZk();

	String create(String path) throws KeeperException, InterruptedException;
	
	int getChildrenSize(String path) throws KeeperException, InterruptedException;
	
	String join(String path) throws KeeperException, InterruptedException;
	
	void joinQueue(String path, int x) throws KeeperException, InterruptedException;
	
	List<String> list(String path) throws KeeperException, InterruptedException;
	
	void deleteAll(String path) throws KeeperException, InterruptedException;
	
	void delete(String path) throws InterruptedException, KeeperException;
	
	String read(String path, Watcher watcher) throws KeeperException, InterruptedException;
	
	void write(String path, String value) throws KeeperException, InterruptedException;

	Stat exists(String path) throws KeeperException, InterruptedException;
}
