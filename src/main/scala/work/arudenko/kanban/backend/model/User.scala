package work.arudenko.kanban.backend.model

import scalikejdbc._
import work.arudenko.kanban.backend.model.User.update
import work.arudenko.kanban.backend.orm.SqlContext.TryLogged
import work.arudenko.kanban.backend.orm.WithCommonSqlOperations

import scala.collection.immutable
import scala.util.Try

/**
 * @param id  for example: ''null''
 * @param firstName  for example: ''null''
 * @param lastName  for example: ''null''
 * @param email  for example: ''null''
 * @param password  for example: ''null''
 * @param phone  for example: ''null''
*/
final case class User (
  id:Int,
  firstName: Option[String],
  lastName: Option[String],
  email: Option[String],
  password: Option[String],
  phone: Option[String],
  enabled:Boolean,
  emailVerified:Boolean,
  admin:Boolean
)

final case class SignUpInfo(
  firstName: Option[String],
  lastName: Option[String],
  email: String,
  password: String,
  phone: Option[String]
)

final case class UserUpdateInfo (
  firstName: Option[String],
  lastName: Option[String],
  email: Option[String],
  password: String,
  newPassword:Option[String],
  phone: Option[String]
)

final case class UserInfo(
  firstName: Option[String],
  lastName: Option[String],
  email: Option[String],
  phone: Option[String]
)

object UserInfo{
  def apply(user:User):UserInfo =
    new UserInfo(
      user.firstName,
      user.lastName,
      user.email,
      user.phone
    )
}

object User extends WithCommonSqlOperations[User] {

  override val tableName = "project_track.peoples"


  override def sqlExtractor(rs: WrappedResultSet): User =
    new User(
      rs.int("id"),
      rs.stringOpt("first_name"),
      rs.stringOpt("last_name"),
      rs.stringOpt("email"),
      rs.stringOpt("password"),
      rs.stringOpt("phone"),
      rs.boolean("is_enabled"),
      rs.boolean("is_email_verified"),
      rs.boolean("is_admin")
    )

  def getLoginUser(email: String): Option[User] = getOne(sql" select * from $table where email=$email and is_enabled=true")

  def getUser(email: String): Option[User] = getOne(sql" select * from $table where email=$email")

  private val u = User.syntax("u")
  def searchUser(userInfo: UserInfo): Seq[User] = getList(
    withSQL {
    select.from(User as u)
      .where(
        sqls.toAndConditionOpt(
          userInfo.firstName.map(fn => sqls.eq(u.column("first_name"), fn)),
          userInfo.lastName.map(fn => sqls.eq(u.column("last_name"), fn)),
          userInfo.email.map(fn => sqls.eq(u.column("email"), fn)),
          userInfo.phone.map(fn => sqls.eq(u.column("phone"), fn))
        )
      )
    }
  )

  def getId(email: String): Option[Int] =
    DB readOnly { implicit session =>
      sql" select id from $table where email=$email ".map(rs => rs.int("id")).single.apply()
    }

  def emailActivateAccount(userId: Int): Int = {
    update(sql" update $table set is_email_verified=true,is_enabled=true where id=$userId")
  }

  def createUsers(users: Seq[UserInfo]): Option[Array[Long]] = Try(DB localTx {
    implicit session =>
      sql"insert into $table (first_name,last_name,email,phone) values(?,?,?,?)"
        .batchAndReturnGeneratedKey(users.map(u => u.productIterator.toSeq)).apply()
  }).toOptionLogInfo


  def setPassword(userId: Int, password: String): Int =
    update(sql" update $table set password=$password where id=$userId")

  def signUp(user: SignUpInfo): Option[Long] = Try(
    insert(
      sql"""
            insert into $table (first_name,last_name,email,password,phone,is_enabled,is_admin) values
            (${user.firstName},${user.lastName},${user.email},${user.password},${user.phone},false,false)
       """)).toOptionLogInfo


  def updateUser(user: User):Option[Int] =
    Try(
    update(
    sql"""
           update $table
           set
           first_name=${user.firstName},
           last_name=${user.lastName},
           email=${user.email},
           phone=${user.phone},
           is_email_verfied=${user.emailVerified},
           is_enabled=${user.enabled},
           is_admin=${user.admin}
           where id=${user.id}""")
  ).toOptionLogInfo


  def updateUser(user: User, userUpdateInfo: UserUpdateInfo): Option[Int] =
  Try(
  update(
    sql"""
           update $table
           set
           first_name=${userUpdateInfo.firstName.getOrElse(user.firstName)},
           last_name=${userUpdateInfo.lastName.orElse(user.lastName)},
           email=${userUpdateInfo.email.orElse(user.email)},
           password=${userUpdateInfo.newPassword.orElse(user.password)},
           phone=${userUpdateInfo.phone.orElse(user.phone)},
           is_email_verfied=${userUpdateInfo.email.fold(user.emailVerified)(_=>false)}
           where id=${user.id}""")
  ).toOptionLogInfo


}
