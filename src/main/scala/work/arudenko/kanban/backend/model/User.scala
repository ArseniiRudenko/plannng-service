package work.arudenko.kanban.backend.model

import scalikejdbc._
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
  firstName: String,
  lastName: Option[String],
  email: Option[String],
  password: Option[String],
  phone: Option[String],
  enabled:Boolean,
  admin:Boolean
)

final case class UserCreationInfo (
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
  firstName: String,
  lastName: Option[String],
  email: Option[String],
  phone: Option[String],
  enabled:Boolean,
  admin:Boolean
)

object UserInfo{
  def apply(user:User):UserInfo =
    new UserInfo(
      user.firstName,
      user.lastName,
      user.email,
      user.phone,
      user.enabled,
      user.admin
    )
}

object User extends WithCommonSqlOperations[User]{

  override val tableName="project_track.peoples"


  override def sqlExtractor(rs: WrappedResultSet): User =
    new User(
      rs.int("id"),
      rs.string("first_name"),
      rs.stringOpt("last_name"),
      rs.stringOpt("email"),
      rs.stringOpt("password"),
      rs.stringOpt("phone"),
      rs.boolean("is_enabled"),
      rs.boolean("is_admin")
    )

  def getLoginUser(email:String): Option[User] = getOne(sql" select * from $table where email=$email and is_enabled=true")

  def getUser(email:String): Option[User] = getOne(sql" select * from $table where email=$email")

  def getId(email:String): Option[Int] =
    DB readOnly { implicit session =>
      sql" select id from $table where email=$email ".map(rs=>rs.int("id")).single.apply()
    }

  def emailActivateAccount(userId:Int): Int = {
    update(sql" update $table set is_email_verified=true,is_enabled=true where id=$userId")
  }

  def createUsers(users:Seq[UserInfo]): Option[Array[Long]] =Try( DB localTx{
    implicit session=>
      sql"insert into $table (first_name,last_name,email,phone,is_enabled,is_admin) values(?,?,?,?,?,?)"
        .batchAndReturnGeneratedKey(users.map(u=>u.productIterator.toSeq)).apply()
  }).toOptionLogInfo


  def setPassword(userId:Int,password:String): Int =
    update(sql" update $table set password=$password where id=$userId")

  def signUp(user:UserCreationInfo): Option[Long] = Try(
    insert(
    sql"""
            insert into $table (first_name,last_name,email,password,phone,is_enabled,is_admin) values
            (${user.firstName},${user.lastName},${user.email},${user.password},${user.phone},false,false)
       """)).toOptionLogInfo


  //TODO: if email updated, email_verified flag should be set to false
  def updateUser(user:User,userUpdateInfo: UserUpdateInfo):Option[Int] =
   Try(
     update(
      sql"""
           update $table
           set
           first_name=${userUpdateInfo.firstName.getOrElse(user.firstName)},
           last_name=${userUpdateInfo.lastName.orElse(user.lastName)},
           email=${userUpdateInfo.email.orElse(user.email)},
           password=${userUpdateInfo.newPassword.orElse(user.password)},
           phone=${userUpdateInfo.phone.orElse(user.phone)}
           where id=${user.id}""")
   ).toOptionLogInfo


}
