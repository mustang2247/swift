package com.ganqiang.swift.net.zk;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

public class ZKOperator implements ZKOperate {

		private static final Charset CHARSET = Charset.forName("UTF-8");
		private static final ArrayList<ACL> ACL_LIST = Ids.OPEN_ACL_UNSAFE;
		private static final CreateMode PERSISTENT = CreateMode.PERSISTENT;
		private ZooKeeper zk;

		public ZooKeeper getZk() {
			return zk;
		}

		public ZKOperator(String host){
				 ZKConnectionManager zkConnection = new ZKConnectionManager();
				 zk = zkConnection.getConnection(host);
		}
		
		public Stat exists(String path) throws KeeperException, InterruptedException {
			 Stat stat = zk.exists(path, true);
			 return stat;
		}

		public String create(String path) throws KeeperException, InterruptedException {
			 Stat stat = zk.exists(path, false);
			 String str = null;
			 if(stat == null){
				 str = zk.create(path, null, ACL_LIST, PERSISTENT);
				}
			 return str;
		}
		
		public String join(String path) throws KeeperException, InterruptedException {
			 String str = zk.create(path, null, ACL_LIST, CreateMode.EPHEMERAL);
			 return str;
		}

		public int getChildrenSize(String groupName) throws KeeperException, InterruptedException {
			 int size = zk.getChildren(groupName, false).size();
			 return size;
	     }

		public void joinQueue(String path, int x) throws KeeperException, InterruptedException {
			 path = path + "/x" + x;
			 zk.create(path, ("x" + x).getBytes(), ACL_LIST, CreateMode.EPHEMERAL_SEQUENTIAL);
	   }

		public List<String> list(String path) throws KeeperException, InterruptedException {
			 Stat stat = zk.exists(path, false);
			 if(stat == null){
				  List<String> children = zk.getChildren(path, false);
					return children;
			   }
				return null;
		}

		public void deleteAll(String path) throws KeeperException, InterruptedException {
				List<String> list = list(path);
				if(list == null || list.size() == 0){
					return;
				}
				for (String child : list) {
						zk.delete(path+"/"+child, -1);
				}
				delete(path);
		}

		public void delete(String path) throws InterruptedException, KeeperException {
				Stat stat = zk.exists(path, false);
				if(stat == null){
					 return;
				 }
				zk.delete(path, -1);
		}

		public String read(String path, Watcher watcher) throws KeeperException, InterruptedException {
				byte[] data = zk.getData(path, watcher, null);
				return new String(data, CHARSET);
		}

		public void write(String path, String value) throws KeeperException, InterruptedException {
				Stat stat = zk.exists(path, false);
				 if(stat == null){
					  zk.create(path, value.getBytes(CHARSET), ACL_LIST, PERSISTENT);
					} else {
						 zk.setData(path, value.getBytes(CHARSET), -1);
				}
		}

}
