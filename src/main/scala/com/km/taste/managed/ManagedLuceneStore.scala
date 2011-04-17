package com.km.taste.managed

import org.apache.lucene.index._
import com.km.taste.command._
import com.km.taste._
import java.io._
import java.nio._
import org.apache.lucene.queryParser.standard._
import org.apache.lucene.queryParser.core._
import org.apache.lucene.search._
import org.apache.lucene.document._
import scala.collection.JavaConversions._

object ManagedLuceneStore {
  def apply(path: File) = {
    new ManagedLuceneStore(ManagedIndex(path))
  }
}

class ManagedLuceneStore(factory: ManagedIndex, codec: LuceneCodec = ThriftLuceneCodec, queryParser: QueryParserHelper = new StandardQueryParser) extends com.km.taste.thrift.Store.Iface {
  val selector = new FieldSelector {
    def accept(s: String) = FieldSelectorResult.LOAD
  }
  
  def reader = factory.getReader
  def searcher = factory.getSearcher
  var writer: ManagedIndexWriter = null
  
  def get(buf: ByteBuffer) = {
    val packet = codec.decode(buf)
    println(packet)
    codec.encode(packet match {
      case LucenePacket(_, Some(query), _, _, _) => {
        val parsed = queryParser.parse(query, "__no_default__").asInstanceOf[Query]
        val td = searcher.search(parsed, 10)
        println(td.scoreDocs)
        val results = td.scoreDocs.map { sd => 
          println(sd.doc.toString)
          val doc = searcher.doc(sd.doc, selector)
          println(doc.getFields)
          val field = doc.getField("default")
          println(field)
          val key = field.stringValue
          Result(key, sd.score) 
        }
        LucenePacket(results = results, counter = Some(td.totalHits))
      }
      case LucenePacket(_, _, _, Some(count), _) => {
        val counter = packet.counter.map{ x => reader.numDocs }
        LucenePacket(counter = counter)
      }
      case _ => LucenePacket()
    })
  }
  
  def put(buf: ByteBuffer) = {
    val packet = codec.decode(buf)
    packet.docs.map { doc => 
      writer.addDocument(doc)
    }
    codec.encode(LucenePacket())
  }
  
  def remove(buf: ByteBuffer) = {
    val packet = codec.decode(buf)
    packet.terms.map { term => 
      writer.deleteDocuments(term)
    }
    packet.query.map { query => 
      val parsed = queryParser.parse(query, "__no_default__").asInstanceOf[Query]
      writer.deleteDocuments(parsed)
    }
    codec.encode(LucenePacket())
  }
  
  def open() = {
    factory.open()
    writer = factory.getWriter
  }
  
  def close() = {
    factory.close()
  }
}