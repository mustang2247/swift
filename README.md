
                Swift
![Image text](https://github.com/ganqiang1983/swift/blob/master/logo.jpg)

  What is it? <br />  
  Swift是一个爬虫工具，它：
  
  1.支持单机和分布式两种运行模式（根据需求选择运行模式） <br /> 
  2.良好的编程接口可以方便抓取任何网站（新站点只需实现Fetchable和Parsable接口） <br /> 
  3.默认每次抓取采用轮询代理和User-Agent的方式，并且支持每两次抓取时的时间间隔，让抓取更像人为操作 <br /> 
  4.支持xml可配置，内置了一些网站<br /> 
  &nbsp;（注：由于某些网站内容会随时改变，具体的实现类也必须要跟着改）<br /> 
  5.多插件式集成：<br /> 
  5.1与Zookeeper继承：维护一组Queue队列来作为分布式爬虫的每次爬取任务的结束点， <br /> 
                                               以便全部爬虫抓取完数据时，通知给其他的任务。 <br /> 
  5.2与HBase继承：可以将爬虫数据存放进HBase中。<br /> 
  5.3与Lucene继承：支持创建索引。<br /> 
  5.4与RDBMS继承：支持将爬虫数据存放进Mysql/Oracle中。<br /> 
  5.5与Thrift继承：通过thrift接口接受swift-monitor发来的指令，还可以通过thrift接口将爬虫数据进行转发。<br />
  -----------

  The Current Version : 1.0-beta

  -----------

  Package : <br />  
  mvn clean package
  
  -----------

  Run : <br />  
  cd target<br />  
  tar xvf swift-version-bin.tar.gz<br />  
  chmod -R 700 swift-version-bin<br />  
  ./swift-version-bin/swift start-local/start-remote/stop
  
  --------------

  Update History
