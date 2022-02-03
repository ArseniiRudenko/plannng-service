package work.arudenko.kanban.backend.orm

import org.postgresql.util.PGInterval
import com.typesafe.config.ConfigFactory
import java.sql.ResultSet

object SqlContext {
  import scalikejdbc._

  import com.typesafe.config.Config
  import com.typesafe.config.ConfigFactory

  val conf: Config = ConfigFactory.load()
  // initialize JDBC driver & connection pool
  Class.forName(conf.getString("jdbc.class"))

  ConnectionPool.singleton(conf.getString("jdbc.url"), conf.getString("jdbc.user"), conf.getString("jdbc.password"))




}
