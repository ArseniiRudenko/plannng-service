package work.arudenko.kanban.backend.model

import scalikejdbc._
import work.arudenko.kanban.backend.orm.WithCommonSqlOperations

/**
 * @param id  for example: ''null''
 * @param firstName  for example: ''null''
 * @param lastName  for example: ''null''
 * @param email  for example: ''null''
 * @param password  for example: ''null''
 * @param phone  for example: ''null''
*/
final case class User (
  id: Option[Int],
  firstName: Option[String],
  lastName: Option[String],
  email: Option[String],
  password: Option[String],
  phone: Option[String],
)

object User extends WithCommonSqlOperations[User]{
  override def sqlExtractor(rs: WrappedResultSet): User =
    new User(
      Some(rs.int("id")),
      Some(rs.string("first_name")),
      rs.stringOpt("last_name"),
      rs.stringOpt("email"),
      rs.stringOpt("password"),
      rs.stringOpt("phone")
    )

  def userInfoExtractor(rs: WrappedResultSet): User =
    new User(
      None,
      Some(rs.string("first_name")),
      rs.stringOpt("last_name"),
      rs.stringOpt("email"),
      None,
      rs.stringOpt("phone")
    )

  def getUserData(email:String): Option[User] = getOne(sql" select * from $tbl where email=$email",userInfoExtractor)

  def getLoginUser(email:String): Option[User] = getOne(sql" select * from $tbl where email=$email and enabled=true")

  def getId(email:String): Option[Int] =
    DB readOnly { implicit session =>
      sql" select id from $tbl where email=$email ".map(rs=>rs.int("id")).single.apply()
    }

}
