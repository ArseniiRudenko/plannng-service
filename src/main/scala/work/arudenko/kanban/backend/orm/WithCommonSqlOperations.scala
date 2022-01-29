package work.arudenko.kanban.backend.orm
import org.postgresql.util.PGInterval
import scalikejdbc._
import scalikejdbc.WrappedResultSet
import work.arudenko.kanban.backend.model.Comment
import work.arudenko.kanban.backend.model.Comment.tableName

import java.sql.ResultSet

trait WithCommonSqlOperations[T] extends SQLSyntaxSupport[T] {


  implicit val PGIntervalTypeBinder: TypeBinder[PGInterval] = new TypeBinder[PGInterval] {
    def apply(rs: ResultSet, label: String): PGInterval = rs.getObject(label).asInstanceOf[PGInterval]
    def apply(rs: ResultSet, index: Int): PGInterval = rs.getObject(index).asInstanceOf[PGInterval]
  }

  def sqlExtractor(rs: WrappedResultSet):T

  def get(id:Int)(implicit session:DBSession): Option[T] =
    sql"select * from $tableName where id=$id".map(rs=>sqlExtractor(rs)).single.apply()



}
