package com.ganqiang.swift.storage.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.ganqiang.swift.storage.Result;
import com.ganqiang.swift.storage.Storable;
import com.ganqiang.swift.util.StringUtil;

public class LuceneIndexStore implements Storable
{
  private static final Logger logger = Logger.getLogger(LuceneIndexStore.class);
  
  private static final Version version = Version.LUCENE_47;
  private static final Analyzer analyzer =  new IKAnalyzer();
  private static final ReentrantLock lock = new ReentrantLock(true);
  private String indexPath;
  private String site;

  public LuceneIndexStore(String site, String indexPath){
    this.site = site;
    this.indexPath = indexPath;
  }

  @Override
  public <Store> Result readOne(Store... s)
  {
    try {
      Result result = (Result) s[0];
      File siteindexfile = new File(indexPath);
      if (siteindexfile.exists() && siteindexfile.listFiles() == null) {
        return null;
      }

      IndexReader reader = DirectoryReader.open(FSDirectory.open(siteindexfile));
      IndexSearcher searcher = new IndexSearcher(reader);
      BooleanQuery booleanQuery = new BooleanQuery();
//      booleanQuery.add(new TermQuery(new Term("name", result.getName())), Occur.MUST);
//      booleanQuery.add(new TermQuery(new Term("site", result.getSite())), Occur.MUST);
      booleanQuery.add(new TermQuery(new Term("site", result.getSite())), Occur.MUST);
//      booleanQuery.add(new TermQuery(new Term("platform", result.getPlatform())), Occur.MUST);

      TopDocs hits = searcher.search(booleanQuery, 1);

      if (hits.totalHits == 1) {
        logger.info("Worker [" + Thread.currentThread().getName() + "] have find ["+ site +"] existing data.");
        Document hit = searcher.doc(hits.scoreDocs[0].doc);
        Result searchResult = new Result();
        searchResult.setName(hit.get("name"));
        searchResult.setSite(hit.get("site"));
        searchResult.setPlatform(hit.get("platform"));
        searchResult.setYearRate(Double.valueOf(hit.get("yearRate")));
        searchResult.setProgress(Double.valueOf(hit.get("progress")));
        searchResult.setStatus(hit.get("status"));
        return searchResult;
      }

    }catch(Exception e){
      logger.error("Worker [" + Thread.currentThread().getName() + "] end build ["+ site +"] index data.");
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <Store> void writeBatch(Store... s)
  {
    lock.lock();
    IndexWriter writer = null;
    try {
      List<Result> results = (List<Result>) s[0];
      boolean iscreate = false;
      File siteindexfile = new File(indexPath);
      if (siteindexfile.exists() && siteindexfile.listFiles() == null) {
        iscreate = true;
      }
      Directory dir = FSDirectory.open(siteindexfile);
      IndexWriterConfig iwc = new IndexWriterConfig(version, analyzer);
      if (iscreate) {
        iwc.setOpenMode(OpenMode.CREATE);
      } else {
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
      }
      iwc.setMaxBufferedDocs(10000);

      writer = new IndexWriter(dir, iwc);
      List<Document> docs = buildDocuments(results);
      if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
        logger.info("Worker [" + Thread.currentThread().getName() + "] create ["+ site +"] index... ");
        writer.addDocuments(docs);
      } else {
        logger.info("Worker [" + Thread.currentThread().getName() + "] update ["+ site +"] index... ");
        Term[] terms = buildTerms(results);
        writer.deleteDocuments(terms);
        writer.addDocuments(docs);
//          writer.forceMergeDeletes();
//          writer.deleteDocuments(new Term("url", result.getUrl()));
//          writer.addDocument(doc);
      }
    
      writer.commit();
    } catch (Exception e){
      logger.info("Worker [" + Thread.currentThread().getName() + "] update ["+ site +"] index... ", e);
    } finally{
      try {
        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      lock.unlock();
    }
  }


  private List<Document> buildDocuments(List<Result> results){
    List<Document> docs = new ArrayList<Document>();
    FieldType ft = new FieldType();
    ft.setIndexed(true);
    ft.setStored(true);
    ft.setTokenized(false);
    for (int i=0; i< results.size(); i++) {
      Result result = results.get(i);
      Document doc = new Document();
      doc.add(new StringField("id", result.getId(), Field.Store.YES));
      doc.add(new TextField("name", result.getName(), Field.Store.YES));
      doc.add(new TextField("site", result.getSite(), Field.Store.YES));
      doc.add(new Field("url", result.getUrl(), ft));
      doc.add(new TextField("platform", result.getPlatform(), Field.Store.YES));
      doc.add(new DoubleField("money", result.getMoney(), Field.Store.YES));
      if (result.getYearRate() != null) {
        doc.add(new DoubleField("yearRate", result.getYearRate(), Field.Store.YES));
      } else if (result.getDayRate() != null) {
        doc.add(new DoubleField("dayRate", result.getDayRate(), Field.Store.YES));
      }
      
      doc.add(new DoubleField("progress", result.getProgress(), Field.Store.YES));
      
      if (!StringUtil.isNullOrBlank(result.getStatus())) {
        doc.add(new TextField("status", result.getStatus(), Field.Store.YES));
      }
      
      docs.add(doc);
    }
    return docs;
  }
  
  private Term[] buildTerms(List<Result> results){
    Term[] terms = new Term[results.size()];
    for (int i=0; i< results.size(); i++) {
      Result result = results.get(i);
      terms[i] = new Term("url", result.getUrl());
    }
    return terms;
  }

}
