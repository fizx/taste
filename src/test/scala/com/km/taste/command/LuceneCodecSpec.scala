package com.km.taste.command

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import com.km.taste._

class LuceneCodecSpec extends AbstractSpec {
  describe("LuceneCodec") {
    
    def roundtrip(obj: LucenePacket) = {
      val codec = ThriftLuceneCodec
      codec.decode(codec.encode(obj)) 
    }
    
    it("should be able to encode and decode an empty lucene command") {
      val obj = LucenePacket()
      roundtrip(obj) should equal (obj)
    }
    
    it("should be able to encode and decode an lucene command with docs") {
      val obj = LucenePacket(docs = Seq(doc("a"), doc("b")))
      roundtrip(obj).docs.length should equal (2)
      roundtrip(obj).docs.first.getField("default").stringValue should equal ("a")
    }
  }
}
