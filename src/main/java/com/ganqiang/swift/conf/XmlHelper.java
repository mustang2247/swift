package com.ganqiang.swift.conf;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public final class XmlHelper
{
  private static final Logger logger = Logger.getLogger(XmlHelper.class);
  /** XML文件路径 */
  private static String XMLPath = null;
  /** XML文档 */
  private static Document document = null;

  public static boolean Validate(String xmlpath, String xsdpath)
  {
    try {
      SchemaFactory schemaFactory = SchemaFactory
          .newInstance("http://www.w3.org/2001/XMLSchema");
      File schemaFile = new File(xsdpath);
      Schema schema = schemaFactory.newSchema(schemaFile);
      Validator validator = schema.newValidator();
      Source source = new StreamSource(xmlpath);
      validator.validate(source);
    } catch (Exception ex) {
      logger.error("xml file is the wrong format.",ex);
    }
    return true;
  }

  /**
   * 打开文档
   */
  public static void openXML()
  {
    try {
      SAXReader reader = new SAXReader();
      document = reader.read(XMLPath);
      System.out.println("openXML() successful ...");
    } catch (Exception e) {
      System.out.println("openXML() Exception:" + e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  public static List<Element> getChildElements(Element element)
  {
    if (element == null) {
      return null;
    } else {
      return element.elements();
    }
  }

  public static Element getChildElementByName(Element parent, String key)
  {
    if (parent == null) {
      return null;
    } else {
      return parent.element(key);
    }
  }

  /**
   * 创建文档
   * 
   * @param rootName
   *          根节点名称
   */
  public static void createXML(String rootName)
  {
    try {
      document = DocumentHelper.createDocument();
      System.out.println("createXML() successful...");
    } catch (Exception e) {
      System.out.println("createXML() Exception:" + e.getMessage());
    }
  }

  public void treeWalk(Element element)
  {
    for (int i = 0, size = element.nodeCount(); i < size; i++) {
      Node node = element.node(i);
      if (node instanceof Element) {
        treeWalk((Element) node);
      } else {

      }
    }
  }


  public void addNodeFromRoot(String nodeName, String nodeValue)
  {
    Element root = document.getRootElement();
    Element level1 = root.addElement(nodeName);
    level1.addText(nodeValue);
  }

  public static Document loadXML(String filePath)
  {
    try {
      SAXReader saxReader = new SAXReader();
      Document doc = saxReader.read(filePath);
      return doc;
    } catch (Exception e) {
      logger.error("xml file " + filePath + " loading success.");
    }
    return null;
  }

  public Element getRootElement(Document doc)
  {
    return doc.getRootElement();
  }

  /**
   * 保存文档
   */
  public static void saveXML(Document document,String path)
  {
    try {
      XMLWriter output = new XMLWriter(new FileWriter(new File(path)));
      output.write(document);
      output.close();
      logger.info("save xml config file successful ...");
    } catch (Exception e1) {
      logger.error("save xml config file throws exception:" + e1.getMessage());
    }
  }

  /**
   * 保存文档
   * 
   * @param toFilePath
   *          保存路径
   */
  public void saveXML(String toFilePath)
  {
    try {
      XMLWriter output = new XMLWriter(new FileWriter(new File(toFilePath)));
      output.write(document);
      output.close();
    } catch (Exception e1) {
      System.out.println("saveXML() Exception:" + e1.getMessage());
    }
  }

  // public List<Element> getElements(Element e){
  // for ( Iterator i = e.elementIterator(); i.hasNext(); ) {
  // Element element = (Element) i.next();
  // // do something
  // }
  // }
  
  public static Element getElement(Node node, String nodeName)
  {
    try {
      Element e = (Element) node.selectSingleNode(nodeName);
      return e;
    } catch (Exception e1) {
      System.out.println("getElementValue() Exception：" + e1.getMessage());
    }
    return null;
  }


  /**
   * 获得某个节点的值
   * 
   * @param nodeName
   *          节点名称
   */
  public static Element getElement(Document doc, String nodeName)
  {
    try {
      Element e = (Element) doc.selectSingleNode(nodeName);
      return e;
    } catch (Exception e1) {
      System.out.println("getElementValue() Exception：" + e1.getMessage());
    }
    return null;
  }

  /**
   * 获得某个节点的子节点的值
   * 
   * @param nodeName
   * @param childNodeName
   * @return
   */
  public static Node getNode(Document doc, String nodeName, String childNodeName)
  {
    try {
      Node node = doc.selectSingleNode("//" + nodeName + "/" + childNodeName);
      return node;
    } catch (Exception e1) {
      System.out.println("getElementValue() Exception：" + e1.getMessage());
      return null;
    }
  }

  /**
   * 设置一个节点的text
   * 
   * @param nodeName
   *          节点名
   * @param nodeValue
   *          节点值
   */
  public static void setElementValue(String nodeName, String nodeValue)
  {
    try {
      Node node = document.selectSingleNode("//" + nodeName);
      node.setText(nodeValue);
    } catch (Exception e1) {
      System.out.println("setElementValue() Exception:" + e1.getMessage());
    }
  }

  /**
   * 设置一个节点值
   * 
   * @param nodeName
   *          父节点名
   * @param childNodeName
   *          节点名
   * @param nodeValue
   *          节点值
   */
  public static void setElementValue(String nodeName, String childNodeName,
      String nodeValue)
  {
    try {
      Node node = document.selectSingleNode("//" + nodeName + "/"
          + childNodeName);
      node.setText(nodeValue);
    } catch (Exception e1) {
      System.out.println("setElementValue() Exception:" + e1.getMessage());
    }
  }
}
