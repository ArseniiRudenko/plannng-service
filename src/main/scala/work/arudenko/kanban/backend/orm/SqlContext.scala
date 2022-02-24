package work.arudenko.kanban.backend.orm

import org.postgresql.util.PGInterval
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import java.sql.ResultSet
import scala.util.{Failure, Success, Try}

object SqlContext  extends LazyLogging{
  import scalikejdbc._

  import com.typesafe.config.Config
  import com.typesafe.config.ConfigFactory

  def intSqlContext(): Unit = {
    val conf: Config = ConfigFactory.load()
    // initialize JDBC driver & connection pool
    Class.forName(conf.getString("jdbc.class"))

    ConnectionPool.singleton(conf.getString("jdbc.url"), conf.getString("jdbc.user"), conf.getString("jdbc.password"))

  }


  implicit class TryLogged[T](val param:Try[T]) extends AnyVal {
    def toOptionLogErr:Option[T] = param match {
      case Failure(exception) => logger.error("exception processing db call",exception);None
      case Success(value) => Some(value)
    }
    def toOptionLogWarn:Option[T] = param match {
      case Failure(exception) => logger.warn("exception processing db call",exception);None
      case Success(value) => Some(value)
    }
    def toOptionLogTrace:Option[T] = param match {
      case Failure(exception) => logger.trace("exception processing db call",exception);None
      case Success(value) => Some(value)
    }

    def toOptionLogInfo:Option[T] = param match {
      case Failure(exception) => logger.info("exception processing db call",exception);None
      case Success(value) => Some(value)
    }
  }

}
