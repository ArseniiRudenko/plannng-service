package work.arudenko.kanban.backend.orm

import com.redis.{RedisClient, RedisClientPool}
import com.typesafe.config.{Config, ConfigFactory}

object RedisContext {

  private val conf: Config = ConfigFactory.load()

  val redis = new RedisClientPool(conf.getString("redis.host"), conf.getInt("redis.port"))


}
