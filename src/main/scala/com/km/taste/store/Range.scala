package com.km.taste.store

import scala.collection._

object Range {
  val EMPTY = new Range {
    def getShards(shards: ShardCollection) = new ShardCollection
  }
}

trait Range {
  def getShards(shards: ShardCollection): ShardCollection
}