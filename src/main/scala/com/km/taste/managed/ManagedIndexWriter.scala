package com.km.taste.managed

import org.apache.lucene.index._
import org.apache.lucene.store._
import org.apache.lucene.analysis._
import org.apache.lucene.analysis.standard._
import org.apache.lucene.document._
import org.apache.lucene.search._
import org.apache.lucene.index._
import java.util.concurrent._
import org.apache.lucene.util.Version._

class ManagedIndexWriter(dir: Directory, cfg: IndexWriterConfig, pool: ExecutorService) extends IndexWriter(dir, cfg) {
  commit()
  def ramCfg = new IndexWriterConfig(LUCENE_31, analyzer)
  val analyzer = new StandardAnalyzer(LUCENE_31)
  var baseReader = IndexReader.open(dir)
  var ramDir = new RAMDirectory
  var ramWriter = new IndexWriter(ramDir, ramCfg)
  ramWriter.commit()
  var ramReader = IndexReader.open(ramDir)
  
  def reopenRam() = { ramWriter.commit(); ramReader = IndexReader.open(ramDir) }
  
  override def addDocument(doc: Document) = addDocument(doc, getAnalyzer)
  override def addDocument(doc: Document, analyzer: Analyzer) = {
    ramWriter.addDocument(doc, analyzer)
    reopenRam()
  }
    
  override def deleteDocuments(query: Query) = {
    ramWriter.deleteDocuments(query)
    reopenRam()
    super.deleteDocuments(query)
    baseReader = super.getReader
  }
  
  override def deleteDocuments(terms: Term*) = {
    ramWriter.deleteDocuments(terms: _*)
    reopenRam()
    super.deleteDocuments(terms: _*)
    baseReader = super.getReader
  }
  
  override def deleteDocuments(term: Term) = {
    ramWriter.deleteDocuments(term)
    reopenRam()
    super.deleteDocuments(term)
    baseReader = super.getReader
  }
  
  override def getReader() = {
    new MultiReader(ramReader, baseReader)
  }
    
  def forceRealtimeToDisk() = {
    val oldRamDir = swapRamDirs
    addIndexes(oldRamDir)
    baseReader = super.getReader
  }
  
  def swapRamDirs() = {
    val newRamDir = new RAMDirectory
    val newRamWriter = new IndexWriter(newRamDir, ramCfg)
    newRamWriter.commit()
    val newRamReader = IndexReader.open(newRamDir)
    val oldRamDir = ramDir
    val oldRamWriter = ramWriter
    val oldRamReader = ramReader
    oldRamWriter.close
    oldRamReader.close
    synchronized {
      ramDir = newRamDir
      ramWriter = newRamWriter
      ramReader = newRamReader
    }
    oldRamDir
  }
  
  def numDocsOnDisk() = baseReader.numDocs
  def numDocsInRAM() = ramReader.numDocs
}