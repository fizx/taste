package com.km.taste.managed

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import org.apache.commons.io._
import org.apache.lucene.document._
import org.apache.lucene.analysis.miscellaneous._
import org.apache.lucene.analysis._
import com.twitter.conversions.time._
import com.twitter.util._
import org.apache.lucene.search._
import org.apache.lucene.index._

class ManagedIndexFactorySpec extends Spec with ShouldMatchers with BeforeAndAfterEach {
  val tmp = new File("/tmp/taste")
  var factory: ManagedIndexFactory = null
  var writer: ManagedIndexWriter = null
  var i = 0

  def doc(s: String) = {
    val d = new Document()
    val t = new Token(s, 0, s.length)
    val ts = new SingleTokenTokenStream(t)
    val f = new Field("default", ts)
    d.add(f)
    d
  }

  def addAndRead(string: String = "hello") = {
    writer.addDocument(doc(string))
    i += 1
    factory.getReader.numDocs() should equal(i)
  }

  override def beforeEach {
    FileUtils.deleteQuietly(tmp)
    tmp.mkdirs()
    factory = ManagedIndexFactory(tmp)
    writer = factory.getWriter
    i = 0
  }

  override def afterEach {
    factory.close()
  }
  
  def inLessThan(d: Duration)(block: => Any) {
    val before = Time.now        
    block
    (Time.now - before) should (be < d)
  }

  describe("A ManagedIndexReader") {

    describe("recurring reads and writes") {
      it("should be able to index and then be updated immediately") {
        addAndRead()
      }

      it("should take less than 5ms per cycle") {
        for (i <- 1 to 100) { inLessThan(1.second) { addAndRead() } } // warm up
        inLessThan(40.millis) {
          for (i <- 1 to 10) { addAndRead() }
        }
      }

      it("should take much less than 1ms to get the reader") {
        val before = Time.now
        for (i <- 1 to 10) { factory.getReader }
        (Time.now - before).inMillis.toInt should (be < 3)
      }

      it("should be able to merge back to the main reader") {
        for (i <- 1 to 10) { addAndRead() }
        writer.forceRealtimeToDisk()
        writer.numDocsOnDisk should equal(i)
        writer.numDocsInRAM should equal(0)
      }

      it("should be able to quickly delete document in RAM") {
        for (i <- 1 to 10) { addAndRead(i.toString) }
        writer.numDocsInRAM should equal(10)
        for (i <- 1 to 10) { 
          writer.deleteDocuments(new Term("default", "1"))
        }
        writer.numDocsInRAM should equal(9)
        
        inLessThan(10.millis) { 
          for (i <- 2 to 9) { 
            writer.deleteDocuments(new Term("default", i.toString))
          }
        }
        writer.numDocsInRAM should equal(1)
        
        writer.forceRealtimeToDisk()
        writer.numDocsInRAM should equal(0)
        writer.numDocsOnDisk should equal(1)
      }
      
      it("should be able to quickly delete document from disk") {
        for (i <- 1 to 10) { addAndRead(i.toString) }
        writer.forceRealtimeToDisk()
        writer.numDocsOnDisk should equal(10)
        writer.deleteDocuments(new Term("default", "1"))
        writer.numDocsOnDisk should equal(9)
      }
    }
  }
}
