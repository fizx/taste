package com.km.taste.sqlite

import com.km.taste.codec._
import com.km.taste.store._
import com.km.taste.util.ByteBuffer._
import java.nio._
import java.sql._
import java.io.File

object SQLiteStore {
  def apply(path: File) = new SQLiteStore(path)
}

class SQLiteStore(val path: File) extends RawStore {
  Class.forName("org.sqlite.JDBC")
  var conn: Connection = null
  
  def get(key: ByteBuffer, timestamp: Long = System.currentTimeMillis)(implicit range: Range) = {
    val stmt = conn.prepareStatement("SELECT v FROM kv WHERE k = ?")
    stmt.setBytes(1, key)
    val rs = stmt.executeQuery()
    val out: ByteBuffer = if (rs.next()) {
      rs.getBytes(1)
    } else {
      ByteBuffer.allocate(0)
    }
    
    stmt.close
    out    
  }
  
  def put(key: ByteBuffer, value: ByteBuffer, timestamp: Long = System.currentTimeMillis)(implicit range: Range) {
    val stmt = conn.prepareStatement("INSERT OR REPLACE INTO kv (k, v) VALUES (?, ?)")
    stmt.setBytes(1, key)
    stmt.setBytes(2, value)
    stmt.executeUpdate()
    stmt.close
  }
  
  def delete(key: ByteBuffer, timestamp: Long = System.currentTimeMillis)(implicit range: Range) {
    val stmt = conn.prepareStatement("DELETE FROM kv WHERE k = ?")
    stmt.setBytes(1, key)
    stmt.executeUpdate()
    stmt.close
  }
  
  def open() = {
    conn = DriverManager.getConnection("jdbc:sqlite:" + path.getAbsolutePath)
    val stmt = conn.createStatement()
    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS kv (k BLOB PRIMARY KEY, v BLOB)")
    stmt.close
  }
  
  def close() = {
    conn.close
  }
  
  def truncate() = {
    val stmt = conn.prepareStatement("DELETE FROM kv")
    stmt.executeUpdate()
    stmt.close
  }
  
  def scanFrom(key: Option[ByteBuffer])(implicit range: Range) = {
    new Iterator[Tuple2[ByteBuffer, ByteBuffer]] {
      def hasNext = false
      def next = (ByteBuffer.allocate(0), ByteBuffer.allocate(0))
      def remove = ()
    }
  }
}