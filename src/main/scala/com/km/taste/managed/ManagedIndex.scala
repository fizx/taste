package com.km.taste.managed

import java.io._
import org.apache.lucene.index._
import org.apache.lucene.store._
import org.apache.lucene.search._
import java.util.concurrent._
import org.apache.lucene.util.Version._
import org.apache.lucene.analysis.standard._
import org.apache.lucene.index.IndexWriterConfig.OpenMode._

object ManagedIndex {
  def stdCfg() = new IndexWriterConfig(LUCENE_31, new StandardAnalyzer(LUCENE_31)).setOpenMode(CREATE_OR_APPEND)
  def apply(path: File,
    cfg: ManagedIndexConfiguration = new DefaultManagedIndexConfiguration,
    pool: ExecutorService = Executors.newCachedThreadPool,
    iwcfg: () => IndexWriterConfig = () => stdCfg()) = {
    new ManagedIndexImpl(path, cfg, pool, iwcfg)
  }
}

/**
 * Gives you an abstraction of an index.  While this is open, it will do things like
 * maintain realtimeness, optimize indexes, flush/commits, etc
 */
trait ManagedIndex {
  def getReader(): IndexReader
  def getSearcher(): IndexSearcher
  def getWriter(): ManagedIndexWriter
  def open(): Unit
  def close(): Unit
}

class ManagedIndexImpl(path: File,
  cfg: ManagedIndexConfiguration,
  pool: ExecutorService,
  iwcfg: () => IndexWriterConfig)
  extends ManagedIndex {

  var dir: Directory = null
  var writer: ManagedIndexWriter = null

  def open() = {
    dir = FSDirectory.open(path)
    writer = new ManagedIndexWriter(dir, iwcfg(), pool)
    writer.commit()
  }

  def getReader = writer.getReader
  def getSearcher = new IndexSearcher(getReader)

  def getWriter = {
    writer
  }

  def close() = {
    writer.close()
  }
}
