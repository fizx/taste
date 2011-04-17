package com.km.taste.command

import java.nio._
import java.io._
import com.km.taste.managed._
import com.km.taste.codec._
import com.km.taste._
import org.apache.lucene.document._
import org.apache.lucene.index._
import org.apache.thrift.protocol._
import org.apache.thrift.transport._
import scala.collection.JavaConversions._

object ThriftLuceneCodec extends LuceneCodec {
  val factory = new TBinaryProtocol.Factory
  def encode(lp: LucenePacket) = {
    val tp = new thrift.LucenePacket
    lp.docs.map { doc => 
      val tdoc = new thrift.Document
      doc.getFields.map { field =>
        val tfield = new thrift.Field
        tfield.setName(field.name)
        tfield.setValue(field.stringValue)
        tdoc.addToFields(tfield)
      }
      tp.addToDocuments(tdoc)
    }
    lp.query.map { query => 
      tp.setQuery(new thrift.Query(query))
    }
    lp.terms.map { term => 
      val tterm = new thrift.Term
      tterm.setName(term.field)
      tterm.setValue(term.text)
      tp.addToTerms(tterm)
    }
    lp.results.map { result => 
      val tresult = new thrift.Result
      tresult.setKey(result.key)
      tresult.setScore(result.score)
      tp.addToResults(tresult)
    }
    lp.counter.map { counter => 
      tp.setCounter(counter)
    }
    val baos = new ByteArrayOutputStream
    val transport = new TIOStreamTransport(baos)
    val protocol = factory.getProtocol(transport)
    tp.write(protocol)
    val out = ByteBuffer.wrap(baos.toByteArray)
    out
  }
  
  def decode(b: ByteBuffer) = {
    val ba = new Array[Byte](b.remaining)
    b.get(ba)
    val bais = new ByteArrayInputStream(ba)
    val transport = new TIOStreamTransport(bais)
    val protocol = factory.getProtocol(transport)
    val tp = new thrift.LucenePacket
    tp.read(protocol)
    val docs = if(tp.isSetDocuments) {
      tp.getDocuments.map { tdoc => 
        val doc = new Document
        tdoc.fields.map { tfield => 
          doc.add(new Field(tfield.getName, tfield.getValue, Field.Store.YES, Field.Index.ANALYZED))
        }
        doc
      }
    } else {
      Seq()
    }
    val query = if(tp.isSetQuery) {
      Some(tp.getQuery.getContent)
    } else {
      None
    }
    val terms = if(tp.isSetTerms) {
      tp.getTerms.map { tterm => 
        new Term(tterm.getName, tterm.getValue)
      }
    } else {
      Seq()
    }    
    val results = if(tp.isSetResults) {
      tp.getResults.map { tresult => 
        new Result(tresult.getKey, tresult.getScore)
      }
    } else {
      Seq()
    }
    val counter = if(tp.isSetCounter) {
      Some(tp.getCounter)
    } else {
      None
    }
    
    new LucenePacket(docs, query, terms, counter, results)
  }
}

trait LuceneCodec extends Codec[LucenePacket]