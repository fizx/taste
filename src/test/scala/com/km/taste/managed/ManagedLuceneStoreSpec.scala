package com.km.taste.managed

import com.km.taste._
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.io._
import org.apache.commons.io._
import com.km.taste.command._
import org.apache.lucene.index._

class ManagedLuceneStoreSpec extends AbstractSpec with ShouldMatchers with BeforeAndAfterEach {
  val tmp = new File("/tmp/taste")
  var store: ManagedLuceneStore = null
  var client: LuceneClient = null
  
  override def beforeEach() {
    FileUtils.deleteQuietly(tmp)
    tmp.mkdirs()
    store = ManagedLuceneStore(tmp)
    store.open()
    client = new LuceneClient(store)
  }
  
  override def afterEach(){
    store.close()
  }
  
  describe("a managed lucene store") {
    it("should be reopenable") {
      store.close()
      store.open()
    }
    
    it("should be able to add and remove docs") {
      client.numDocs should be (0)
      client.add(doc("hello"))
      client.numDocs should be (1)
      client.delete(new Term("default", "hello"))
      client.numDocs should be (0)
    }
    
    it("should be able to query") {
      client.add(doc("hello"))
      client.search("default:hello").first.key should equal ("hello")
    }
    
    it("should be able to remove by query") {
      client.numDocs should be (0)
      client.add(doc("hello"))
      client.numDocs should be (1)
      client.delete("default:hello")
      client.numDocs should be (0)
    }
  }
}
