package com.km.taste.codec

import java.nio._

trait Codec[T] {
  def encode(t: T): ByteBuffer
  def decode(b: ByteBuffer): T
}