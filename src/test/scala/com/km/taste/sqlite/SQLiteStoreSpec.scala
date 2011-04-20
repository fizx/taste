package com.km.taste.sqlite

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import com.km.taste._
import com.km.taste.store._
import org.apache.commons.io._
import com.km.taste.util.ByteBuffer._

class SQLiteStoreSpec extends AbstractSpec {
  val tmp = new File("/tmp/taste-sqlite")
  var store: SQLiteStore = null
  implicit val range = Range.EMPTY
  
  override def beforeEach {
    FileUtils.deleteQuietly(tmp)
    store = SQLiteStore(tmp)
    store.open()
  }

  override def afterEach {
    store.close()
  }
  
  // Strings are implicitly converted to their byte content
  describe("A SQLiteStore") {
    it("should be readable and writable") {    
      store.put("hello", "world")
      buf2s(store.get("hello")) should equal ("world")
    }
    
    it("should be deletable") {
      store.put("hello", "world")
      store.delete("hello")
      buf2s(store.get("hello")) should equal ("")
    }

    it("should be truncatable") {
      store.put("hello", "world")
      store.truncate()
      buf2s(store.get("hello")) should equal ("")
    }
    
    
  }
}
