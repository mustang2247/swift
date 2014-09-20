package com.ganqiang.swift.net.zk;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.core.Event;
import com.ganqiang.swift.core.Process;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.util.StringUtil;

public class ZKClient implements Process{
		
	 private static final Logger logger = Logger.getLogger(ZKClient.class);
	 private static final String PATH = "/queue";
	 private static final String START_PATH = "/queue/start";
		private ZKOperate zkop = null;
		
		public ZKClient(){
		}

		public void initQueue(int seqid, int totalnodes) throws KeeperException, InterruptedException{
			Stat stat = zkop.exists(START_PATH);
			if(stat != null){
				zkop.deleteAll(PATH);
			}
			zkop.create(PATH);
			zkop.joinQueue(PATH, seqid);
			int length = zkop.getChildrenSize(PATH);
			if (length >= totalnodes){
				 zkop.join(START_PATH);
			  logger.info("swift creates "+START_PATH+" success !");
			}
		}

		@Override
		public void execute(Event event) {
			logger.info("Worker [" + Thread.currentThread().getName() + "] begin conect zookeeper.");
			Seed seed = (Seed) event.get(Event.seed_key);
		  String instanceid = seed.getId();
		  String address =  Constants.local_address_map.get(instanceid);
		  int seqid = 0;
		  int totalnodes = 0;
		  if(StringUtil.isNullOrBlank(address)){
			   address = Constants.remote_address;
			   seqid = Constants.remote_seq_id;
			   totalnodes =Constants.remote_total_nodes;
		  }else{
			  seqid = Constants.local_seq_id_map.get(instanceid);
			  totalnodes =Constants.local_total_nodes_map.get(instanceid);
		     }
		  zkop = new ZKOperator(address);
			try {
				initQueue(seqid, totalnodes);
//				zkop.getZk().close();
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.info("Worker [" + Thread.currentThread().getName() + "] end connect zookeeper.");
		}
		
}
