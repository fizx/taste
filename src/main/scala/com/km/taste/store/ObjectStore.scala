package com.km.taste.store

trait ObjectStore[K, V]{
  def get(key: K, timestamp: Long)(implicit range: Range): V
  def put(key: K, value: V, timestamp: Long)(implicit range: Range): Unit
  def delete(key: K, timestamp: Long)(implicit range: Range): Unit
  def scanFrom(key: Option[K])(implicit range: Range): Iterator[Tuple2[K, V]]
  def truncate(): Unit
  def open(): Unit
  def close(): Unit
}