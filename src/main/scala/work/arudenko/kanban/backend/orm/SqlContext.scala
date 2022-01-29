package work.arudenko.kanban.backend.orm

import org.postgresql.util.PGInterval
import com.typesafe.config.ConfigFactory
import java.sql.ResultSet

object SqlContext {
  import scalikejdbc._

  import com.typesafe.config.Config
  import com.typesafe.config.ConfigFactory

  val conf: Config = ConfigFactory.load("jdbc")
  // initialize JDBC driver & connection pool
  Class.forName("org.h2.Driver")

  ConnectionPool.singleton(conf.getString("url"), conf.getString("user"), conf.getString("password"))




}
