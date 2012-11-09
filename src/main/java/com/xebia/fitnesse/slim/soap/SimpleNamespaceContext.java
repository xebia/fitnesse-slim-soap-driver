package com.xebia.fitnesse.slim.soap;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.namespace.NamespaceContext;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class SimpleNamespaceContext implements NamespaceContext {

  private BiMap<String, String> namespaceByPrefix = HashBiMap.create();

  public void addNamespacePrefix(String prefix, String namespace) {
    namespaceByPrefix.put(prefix, namespace);
  }

  public String getNamespaceURI(String prefix) {
    return namespaceByPrefix.get(prefix);
  }

  public String getPrefix(String namespaceURI) {
    return namespaceByPrefix.inverse().get(namespaceURI);
  }

  @SuppressWarnings("rawtypes")
  public Iterator getPrefixes(String namespaceURI) {
    return Collections.singleton(getPrefix(namespaceURI)).iterator();
  }

  public void clear() {
    namespaceByPrefix.clear();
  }
  
  public Iterable<Entry<String,String>> allNamespaces() {
    return namespaceByPrefix.entrySet();
  }

}