package com.ganqiang.swift.net.zk;

import com.ganqiang.swift.conf.RemoteConfig;
import com.ganqiang.swift.conf.LocalConfig.Instance;
import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.prep.Visitable;
import com.ganqiang.swift.prep.Visitor;

public class ZKContextHandler implements Visitable{

  public void localInit(Instance instance){
	  Constants.local_seq_id_map.put(instance.getId(), instance.getSeqId());
	  Constants.local_address_map.put(instance.getId(), instance.getAddress());
	  Constants.local_total_nodes_map.put(instance.getId(), instance.getTotalNodes());
	  }

  public void remoteInit(){
	  RemoteConfig config = Constants.remote_config;
	  Constants.remote_seq_id = config.getSeqId();
	  Constants.remote_address = config.getAddress();
	  Constants.remote_total_nodes = config.getTotalNodes();
	  }

	 @Override
	 public void accept(Visitor visitor) {
		  visitor.visitZKContext(this);
	  }

}
