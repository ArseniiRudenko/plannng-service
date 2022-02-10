package work.arudenko.kanban.backend.orm
import org.postgresql.util.PGInterval
import scalikejdbc._
import scalikejdbc.WrappedResultSet
import java.sql.ResultSet

trait WithCommonSqlOperations[T] extends SQLSyntaxSupport[T] {
  SqlContext

  implicit val PGIntervalTypeBinder: TypeBinder[PGInterval] = new TypeBinder[PGInterval] {
    def apply(rs: ResultSet, label: String): PGInterval = rs.getObject(label).asInstanceOf[PGInterval]
    def apply(rs: ResultSet, index: Int): PGInterval = rs.getObject(index).asInstanceOf[PGInterval]
  }

  protected def getList(sql:SQL[T,NoExtractor],extractor: WrappedResultSet=>T = sqlExtractor): List[T] =
    DB readOnly { implicit session =>
      sql.map(rs => extractor(rs)).list.apply()
    }

  protected def update(sql:SQL[T,NoExtractor]): Int =
    DB autoCommit { implicit session =>
      sql.update.apply()
    }
  protected def insert(sql:SQL[T,NoExtractor]): Long =
    DB autoCommit { implicit session =>
      sql.updateAndReturnGeneratedKey.apply()
    }

  protected def getOne(sql:SQL[T,NoExtractor],extractor: WrappedResultSet=>T = sqlExtractor): Option[T] =
    DB readOnly { implicit session =>
      sql.map(rs => extractor(rs)).single.apply()
    }

  def sqlExtractor(rs: WrappedResultSet):T

  def delete(id:Int): Int = update(sql"delete from $table where id=$id")

  def get(id:Int): Option[T] = getOne(sql"select * from $table where id=$id")

}
