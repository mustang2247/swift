package com.ganqiang.swift.conf;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.ganqiang.swift.conf.LocalConfig.Instance;
import com.ganqiang.swift.core.Constants;
import com.ganqiang.swift.net.http.HttpHelper;
import com.ganqiang.swift.net.http.HttpProxy;
import com.ganqiang.swift.prep.Visitable;
import com.ganqiang.swift.prep.Visitor;
import com.ganqiang.swift.seed.Seed;
import com.ganqiang.swift.util.CalculateUtil;
import com.ganqiang.swift.util.DateUtil;
import com.ganqiang.swift.util.StringUtil;

public class LocalConfigHandler extends AbstractConfig implements Visitable {
	private static final Logger logger = Logger
			.getLogger(LocalConfigHandler.class);

	private final String local_xml = System.getProperty("user.dir")
			+ "/conf/swift-local.xml";
	private final String local_xsd = System.getProperty("user.dir")
			+ "/conf/swift-local.xsd";

	@Override
	String getConfigFile() {
		return local_xml;
	}

	@Override
	String getValidateFile() {
		return local_xsd;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loading() {
		LocalConfig localConfig = new LocalConfig();
		Element swiftNode = XmlHelper.getElement(getDocument(), swift_node);
		Node threadNumNode = swiftNode.selectSingleNode(thread_num_node);
		if (threadNumNode != null) {
			String threadNum = threadNumNode.getText().trim();
			localConfig.setThreadNum(Integer.valueOf(threadNum));
			Constants.thread_num = localConfig.getThreadNum();
		}
		List<Element> instanceNodes = swiftNode.selectNodes(instance_node);
		List<Instance> instances = new ArrayList<Instance>();
		for (Element instance : instanceNodes) {
			Instance xmlobj = new Instance();
			String idvalue = instance.attributeValue(id_node);
			xmlobj.setId(idvalue);
			Node timerNode = instance.selectSingleNode(timer_node);
			if (timerNode != null) {
				String starttimevalue = timerNode.selectSingleNode(
						start_time_node).getText();
				String intervalvalue = timerNode
						.selectSingleNode(interval_node).getText();
				Long interval = null;
				if (StringUtil.isNullOrBlank(intervalvalue)
						|| intervalvalue.equalsIgnoreCase("0")) {
					interval = null;
				} else if (!CalculateUtil.isNumeric(intervalvalue)) {
					interval = Long.valueOf(CalculateUtil
							.parseExp(intervalvalue));
				} else {
					interval = Long.valueOf(intervalvalue);
				}
				boolean flag = DateUtil.checkDate(starttimevalue);
				Date stime = null;
				if (flag) {
					stime = DateUtil.parse(starttimevalue);
					if (stime.before(new Date())) {
						logger.error("swift-local.xml file configuration error : instance ["
								+ idvalue + "] starttime have expired");
						System.exit(1);
					}
				} else {
					logger.error("swift-local.xml file configuration error : <starttime> of instance id="
							+ idvalue);
					System.exit(1);
				}
				xmlobj.setInterval(interval);
				xmlobj.setStartTime(stime);
			}
			Node zkNode = instance.selectSingleNode(zookeeper_node);
			if (zkNode != null) {
				Node seqidNode = zkNode.selectSingleNode(seq_id_node);
				if (seqidNode == null) {
					logger.error("swift-local.xml file configuration error : <zookeeper> has not <seq_id> element.");
					System.exit(1);
				}
				Node addressNode = zkNode.selectSingleNode(address_node);
				if (addressNode == null) {
					logger.error("swift-local.xml file configuration error : <zookeeper> has not <address> element.");
					System.exit(1);
				}
				Node totalnodesNode = zkNode.selectSingleNode(total_nodes_node);
				if (totalnodesNode == null) {
					logger.error("swift-local.xml file configuration error : <zookeeper> has not <total_nodes> element.");
					System.exit(1);
				}
				String seqid = seqidNode.getText();
				String address = addressNode.getText();
				String totalNodes = totalnodesNode.getText();
				if (!StringUtil.isNullOrBlank(seqid)) {
					xmlobj.setSeqId(Integer.valueOf(seqid));;
				} else {
					logger.error("swift-local.xml file configuration error : <zookeeper> has not <seq_id> element value.");
					System.exit(1);
				}
				if (!StringUtil.isNullOrBlank(address)) {
					xmlobj.setAddress(address);;
				} else {
					logger.error("swift-local.xml file configuration error : <zookeeper> has not <address> element value.");
					System.exit(1);
				}
				if (!StringUtil.isNullOrBlank(totalNodes)) {
					xmlobj.setTotalNodes(Integer.valueOf(totalNodes));;
				} else {
					logger.error("swift-local.xml file configuration error : <zookeeper> has not <total_nodes> element value.");
					System.exit(1);
				}
			}

			Node proxyNode = instance.selectSingleNode(http_proxy_node);
			if (proxyNode != null
					&& !proxyNode.getStringValue().trim().equals("")) {
				String[] proxys = proxyNode.getStringValue().trim()
						.split("\\|");
				List<HttpProxy> nproxys = new ArrayList<HttpProxy>();
				for (int i = 0; i < proxys.length; i++) {
					String proxy = proxys[i].trim();
					HttpProxy httpProxy = null;
					if (proxy.contains(",")) {
						String[] userpwdproxy = proxy.split("\\,");
						String hp = userpwdproxy[0].trim();
						String up = userpwdproxy[1].trim();
						String[] hppart = hp.split("\\:");
						String[] uppart = up.split("\\:");
						HttpHost host = new HttpHost(hppart[0].trim(),
								Integer.valueOf(hppart[1].trim()));
						httpProxy = new HttpProxy(host, uppart[0].trim(),
								uppart[1].trim());
					} else {
						String[] hppart = proxy.split("\\:");
						HttpHost host = new HttpHost(hppart[0].trim(),
								Integer.valueOf(hppart[1].trim()));
						httpProxy = new HttpProxy(host);
					}
					nproxys.add(httpProxy);
				}
				xmlobj.setHttpProxys(nproxys);
			}

			Node seedsNode = instance.element(seeds_node);
			String inseedsvalue = null;
			String outseedsvalue = null;
			if (seedsNode == null) {
				logger.error("swift-local.xml file configuration error : <instance> has not <seeds> element of instance id="
						+ idvalue + ".");
				System.exit(1);
			}

			Node inseeds = seedsNode.selectSingleNode(inside_node);
			Node outseeds = seedsNode.selectSingleNode(outside_node);
			if (inseeds == null && outseeds == null) {
				logger.error("swift-local.xml file configuration error : <seeds> has not children elements of instance id="
						+ idvalue + ".");
				System.exit(1);
			}
			if (inseeds != null && inseeds.hasContent()) {
				Element inele = (Element) inseeds;
				if (inele.hasContent()) {
					inseedsvalue = inele.getStringValue().trim();
					if (!StringUtil.isNullOrBlank(inseedsvalue)) {
						if (inseedsvalue.equalsIgnoreCase("all")) {
							xmlobj.setInSeeds(Seed.getAllInsites());
						} else {
							String[] seeds = inseedsvalue.split("\\|");
							String[] nseeds = new String[seeds.length];
							for (int i = 0; i < seeds.length; i++) {
								String seed = seeds[i].trim();
								nseeds[i] = seed;
								logger.info("instance [" + idvalue
										+ "] loading inside seed [" + seed
										+ "]");
							}
							xmlobj.setInSeeds(nseeds);
						}
					}
					String isdown = inele.attributeValue(is_download_node);
					if (!StringUtil.isNullOrBlank(isdown)) {
						xmlobj.setInIsdownload(Boolean.valueOf(isdown));
					}
					String jssupport = inele.attributeValue(js_support_node);
					if (!StringUtil.isNullOrBlank(jssupport)) {
						xmlobj.setJssupport(Boolean.valueOf(jssupport));
					}
					Constants.js_support_map.put(idvalue, xmlobj.isJssupport());
					String indelay = inele.attributeValue(delay_node);
					if (!StringUtil.isNullOrBlank(indelay)) {
						xmlobj.setIndelay(Long.valueOf(indelay));
					}
					Constants.inside_delay_map
							.put(idvalue, xmlobj.getIndelay());
					String useProxy = inele.attributeValue(use_proxy_node);
					if (!StringUtil.isNullOrBlank(useProxy)) {
						boolean useproxy = Boolean.valueOf(useProxy);
						if (useproxy
								&& (proxyNode == null || proxyNode
										.getStringValue().trim().equals(""))) {
							logger.error("swift-local.xml file configuration error : instance id="
									+ idvalue
									+ " can not set use_proxy=true cause "
									+ "<http_proxy> has not contents .");
							System.exit(1);
						}
						xmlobj.setInUseProxy(useproxy);
					}
					Constants.inside_use_proxy_map.put(idvalue,
							xmlobj.isInUseProxy());
				} else {
					logger.error("swift-local.xml file configuration error : <seeds>-<inside> has not contents of instance id="
							+ idvalue + ".");
					System.exit(1);
				}
			}
			if (outseeds != null && outseeds.hasContent()) {
				Element outele = (Element) outseeds;
				if (outele.hasContent()) {
					outseedsvalue = outele.getStringValue().trim();
					if (!StringUtil.isNullOrBlank(outseedsvalue)) {
						String[] array = outseedsvalue.split("\\|");
						String[] nseeds = new String[array.length];
						for (int i = 0; i < nseeds.length; i++) {
							String seed = array[i].trim();
							seed = HttpHelper.formatOutSideUrl(seed);
							nseeds[i] = seed;
							logger.info("instance [" + idvalue
									+ "] loading outside seed [" + seed + "]");
						}
						xmlobj.setOutSeeds(nseeds);
					}
					String isdown = outele.attributeValue(is_download_node);
					if (!StringUtil.isNullOrBlank(isdown)) {
						xmlobj.setOutIsdownload(Boolean.valueOf(isdown));
					}
					String iscascade = outele.attributeValue(is_cascade_node);
					if (!StringUtil.isNullOrBlank(iscascade)) {
						xmlobj.setCascade(Boolean.valueOf(iscascade));
					}
					String depth = outele.attributeValue(depth_node);
					if (!StringUtil.isNullOrBlank(depth)) {
						xmlobj.setDepth(Integer.valueOf(depth));
						Constants.depth_map.put(idvalue, xmlobj.getDepth());
					}
					String outdelay = outele.attributeValue(delay_node);
					if (!StringUtil.isNullOrBlank(outdelay)) {
						xmlobj.setIndelay(Long.valueOf(outdelay));
					}
					Constants.outside_delay_map.put(idvalue,
							xmlobj.getOutdelay());
					String useProxy = outele.attributeValue(use_proxy_node);
					if (!StringUtil.isNullOrBlank(useProxy)) {
						boolean useproxy = Boolean.valueOf(useProxy);
						if (useproxy
								&& (proxyNode == null || proxyNode
										.getStringValue().trim().equals(""))) {
							logger.error("swift-local.xml file configuration error : instance id="
									+ idvalue
									+ " can not set use_proxy=true cause "
									+ "<http_proxy> has not contents .");
							System.exit(1);
						}
						xmlobj.setOutUseProxy(useproxy);
					}
					Constants.outside_use_proxy_map.put(idvalue,
							xmlobj.isOutUseProxy());
				} else {
					logger.error("swift-local.xml file configuration error : <seeds>-<inside> has not contents of instance id="
							+ idvalue + ".");
					System.exit(1);
				}
			}

			Node storageNode = instance.selectSingleNode(storage_node);
			if (storageNode == null) {
				logger.error("swift-local.xml file configuration error : <instance> has not <storage> element of instance id="
						+ idvalue + ".");
				System.exit(1);
			}
			Node disk = storageNode.selectSingleNode(disk_node);
			if (disk == null || StringUtil.isNullOrBlank(disk.getStringValue())) {
				xmlobj.setDisk(default_disk_path);
			} else {
				xmlobj.setDisk(disk.getStringValue().trim());
			}
			Constants.disk_path_map.put(idvalue, xmlobj.getDisk());

			Element inele = (Element) disk;
			String isSync = inele.attributeValue(is_sync_node);
			if (!StringUtil.isNullOrBlank(isSync)) {
				xmlobj.setSync(Boolean.valueOf(isSync));
				// if (xmlobj.isSync()) {
				// Constants.DISK_PATH_MAP.put(idvalue, xmlobj.getDisk());
				// }
			}

			String syncDomain = inele.attributeValue(sync_domain_node);
			if (!StringUtil.isNullOrBlank(syncDomain)) {
				xmlobj.setSyncDomain(syncDomain);
				if (xmlobj.isSync()) {
					Constants.sync_domain_map.put(idvalue,
							xmlobj.getSyncDomain());
				}
			}
			Constants.sync_map.put(idvalue, xmlobj.isSync());

			Node node = storageNode.selectSingleNode(thrift_server_node);
			if (node != null && node.hasContent()) {
				xmlobj.setThriftServer(node.getText().trim());
				Constants.local_ts_map.put(idvalue, xmlobj.getThriftServer());
			}

			Node dbNode = storageNode.selectSingleNode(db_node);
			if (dbNode != null) {
				xmlobj.setDbDriver(dbNode.selectSingleNode(driver_node)
						.getText().trim());
				xmlobj.setDbUrl(dbNode.selectSingleNode(url_node).getText()
						.trim());
				xmlobj.setDbUsername(dbNode.selectSingleNode(user_name_node)
						.getText().trim());
				xmlobj.setDbPassword(dbNode.selectSingleNode(password_node)
						.getText().trim());
				xmlobj.setDbPoolSize(Integer.valueOf(dbNode
						.selectSingleNode(pool_size_node).getText().trim()));
			}

			Node index = storageNode.selectSingleNode(index_node);
			if (index != null) {
				if (StringUtil.isNullOrBlank(index.getStringValue())) {
					xmlobj.setIndex(default_index_path);
				} else {
					xmlobj.setIndex(index.getStringValue().trim());
				}
				Constants.inside_index_path_map.put(idvalue, xmlobj.getIndex());
			}
			instances.add(xmlobj);
		}
		localConfig.setInstances(instances);
		Constants.local_config = localConfig;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visitLocalConfig(this);
	}

	@SuppressWarnings("unchecked")
	public void updateTime(String instanceid) {
		Document doc = getDocument();
		if (doc == null) {
			return;
		}
		Element swiftNode = XmlHelper.getElement(doc, swift_node);
		List<Element> instanceNodes = swiftNode.selectNodes(instance_node);
		for (Element instance : instanceNodes) {
			String idvalue = instance.attributeValue(id_node);
			if (!instanceid.equals(idvalue)) {
				continue;
			}
			Node timerNode = instance.selectSingleNode(timer_node);
			if (timerNode != null) {
				Node stnode = timerNode.selectSingleNode(start_time_node);
				String starttimevalue = stnode.getText();
				String intervalvalue = timerNode
						.selectSingleNode(interval_node).getText();
				Long interval = null;
				if (StringUtil.isNullOrBlank(intervalvalue)
						|| intervalvalue.equalsIgnoreCase("0")) {
					interval = null;
				} else {
					if (!CalculateUtil.isNumeric(intervalvalue)) {
						interval = Long.valueOf(CalculateUtil
								.parseExp(intervalvalue));
					} else {
						interval = Long.valueOf(intervalvalue);
					}
					Date stime = DateUtil.parse(starttimevalue);
					String nextdate = DateUtil.getNextDate(stime, interval);
					stnode.setText(nextdate);
					XmlHelper.saveXML(doc, local_xml);
				}

			}
		}
	}

}
