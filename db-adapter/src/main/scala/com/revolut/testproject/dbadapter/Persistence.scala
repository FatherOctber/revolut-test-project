package com.revolut.testproject.dbadapter

trait Persistence[K, V] {
  /**
    * push to db
    * @param key
    * @param data
    */
  def <<(key: K, data: V): Unit

  /**
    * pull from db
    * @param key
    * @return value at specified key
    */
  def >>(key: K): V

  /**
    * compare-and-set operation
    * @param update - update actual data
    * @param key - key
    */
  def update(update: (V) => V, key: K): Unit

}
