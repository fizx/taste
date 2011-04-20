package com.km.taste.util

import java.nio.{ByteBuffer => JBB}

object ByteBuffer { 
  implicit def buf2a(b: JBB) = {
    val a = new Array[Byte](b.remaining)
    b.get(a)    
    a
  }
  
  implicit def a2buf(a: Array[Byte]) = JBB.wrap(a)
  
  implicit def s2buf(s: String) = JBB.wrap(s.getBytes)
  
  implicit def buf2s(b: JBB) = {
    val a = new Array[Byte](b.remaining)
    b.get(a)
    new String(a)
  }
}