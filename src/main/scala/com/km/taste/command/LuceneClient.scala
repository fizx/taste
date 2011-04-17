package com.km.taste.command

import com.km.taste.managed._
import org.apache.lucene.index._
import org.apache.lucene.document._

class LuceneClient(store: ManagedLuceneStore, codec: LuceneCodec = ThriftLuceneCodec) {
  def add(doc: Document) = {
    val p = LucenePacket(docs = Seq(doc))
    val buf = store.put(codec.encode(p))
    codec.decode(buf)
  }
  
  def delete(term: Term) = {
    val p = LucenePacket(terms = Seq(term))
    val buf = store.remove(codec.encode(p))
    codec.decode(buf)
  }
  
  def delete(query: String) = {
    val p = LucenePacket(query = Some(query))
    val buf = store.remove(codec.encode(p))
    codec.decode(buf)
  }
  
  def search(query: String) = {
    val p = LucenePacket(query = Some(query))
    val buf = store.get(codec.encode(p))
    codec.decode(buf).results
  }
  
  def numDocs() = {
    val p = LucenePacket(counter = Some(1))
    val buf = store.get(codec.encode(p))
    codec.decode(buf).counter.get
  }
}