package com.km.taste.managed

import java.io._
import org.apache.lucene.index._
import org.apache.lucene.store._
import java.util.concurrent._
import org.apache.lucene.util.Version._
import org.apache.lucene.analysis.standard._
import org.apache.lucene.index.IndexWriterConfig.OpenMode._

object ManagedIndexFactory {
  def stdCfg() = new IndexWriterConfig(LUCENE_31, new StandardAnalyzer(LUCENE_31)).setOpenMode(CREATE_OR_APPEND)
  def apply(path: File,
    cfg: ManagedIndexConfiguration = new DefaultManagedIndexConfiguration,
    pool: ExecutorService = Executors.newCachedThreadPool,
    iwcfg: () => IndexWriterConfig = () => stdCfg()) = {
    new ManagedIndexFactoryImpl(path, cfg, pool, iwcfg)
  }
}

/**
 * Gives you an abstraction of an index.  While this is open, it will do things like
 * maintain realtimeness, optimize indexes, flush/commits, etc
 */
trait ManagedIndexFactory {
  def getReader(): IndexReader
  def getWriter(): ManagedIndexWriter
  def close(): Unit
}

class ManagedIndexFactoryImpl(path: File,
  cfg: ManagedIndexConfiguration,
  pool: ExecutorService,
  iwcfg: () => IndexWriterConfig)
  extends ManagedIndexFactory {

  val dir = FSDirectory.open(path)
  val writer = new ManagedIndexWriter(dir, iwcfg(), pool)
  writer.commit()

  def getReader = {
    writer.getReader
  }

  def getWriter = {
    writer
  }

  def close() = {
    writer.close()
  }
}
