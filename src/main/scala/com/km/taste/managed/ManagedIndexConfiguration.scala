package com.km.taste.managed

trait ManagedIndexConfiguration {
  def bufferSize: Int
}

class DefaultManagedIndexConfiguration extends ManagedIndexConfiguration {
  def bufferSize = 10000
}