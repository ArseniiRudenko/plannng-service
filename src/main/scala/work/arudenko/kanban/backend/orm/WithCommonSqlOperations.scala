package work.arudenko.kanban.backend.orm
import org.postgresql.util.PGInterval
import scalikejdbc._
import scalikejdbc.WrappedResultSet
import work.arudenko.kanban.backend.model.Comment.{sqlExtractor, tbl}

import java.sql.ResultSet

trait WithCommonSqlOperations[T] extends SQLSyntaxSupport[T] {
  SqlContext

  implicit val PGIntervalTypeBinder: TypeBinder[PGInterval] = new TypeBinder[PGInterval] {
    def apply(rs: ResultSet, label: String): PGInterval = rs.getObject(label).asInstanceOf[PGInterval]
    def apply(rs: ResultSet, index: Int): PGInterval = rs.getObject(index).asInstanceOf[PGInterval]
  }

  protected def getList(sql:SQL[T,NoExtractor])=DB readOnly { implicit session =>
    sql.map(rs => sqlExtractor(rs)).list.apply()
  }

  protected def getOne(sql:SQL[T,NoExtractor])=DB readOnly { implicit session =>
    sql.map(rs => sqlExtractor(rs)).single.apply()
  }

  def sqlExtractor(rs: WrappedResultSet):T
  protected val curSyntax: scalikejdbc.QuerySQLSyntaxProvider[scalikejdbc.SQLSyntaxSupport[T], T] = syntax("m")

  protected def tbl: scalikejdbc.TableAsAliasSQLSyntax =this.as(curSyntax)

  def get(id:Int): Option[T] = getOne(sql"select * from $tbl where id=$id")



}
