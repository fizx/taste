package com.km.taste.store

import java.nio._

trait Store {
  def get(range: Range, query: ByteBuffer, timestamp: Long): ByteBuffer
  def put(range: Range, query: ByteBuffer, timestamp: Long): ByteBuffer
  def delete(range: Range, query: ByteBuffer, timestamp: Long): ByteBuffer
}