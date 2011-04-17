package com.km.taste.command

import java.io._
import org.apache.lucene.search._
import org.apache.lucene.index._
import org.apache.lucene.document._

object LucenePacket {
  def add(doc: Document) = LucenePacket(docs = Seq(doc))
}

case class LucenePacket(docs: Seq[Document] = Seq(), query: Option[String] = None, terms: Seq[Term] = Seq(), counter: Option[Int] = None, results: Seq[Result] = Seq())
