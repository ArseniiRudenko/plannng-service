package work.arudenko.kanban.backend.model

import scalikejdbc._
import work.arudenko.kanban.backend.model.Comment.syntax
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
  firstName: String,
  lastName: Option[String],
  email: String,
  password: String,
  phone: Option[String]
)

final case class UserUpdateInfo (
  firstName: Option[String],
  lastName: Option[String],
  email: String,
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

  def getLoginUser(email:String): Option[User] = getOne(sql" select * from $table where email=$email and enabled=true")

  def getUser(email:String): Option[User] = getOne(sql" select * from $table where email=$email")

  def getId(email:String): Option[Int] =
    DB readOnly { implicit session =>
      sql" select id from $table where email=$email ".map(rs=>rs.int("id")).single.apply()
    }

  def emailActivateAccount(userId:Int) = {
    update(sql" update $table set is_email_verified=true,is_enabled=true where id=$userId")
  }

  def createUsers(users:Seq[UserInfo]) = ???

  def createUser(users:UserInfo) = ???

  def setPassword(userId:Int,password:String)=
    update(sql" update $table set password=${password} where id=${userId}")

  def signUp(user:UserCreationInfo) = insert(
    sql"""
            insert into $table (first_name,last_name,email,password,phone,is_enabled,is_admin) values
            (${user.firstName},${user.lastName},${user.email},${user.password},${user.phone},false,false)
       """)

  def updateUser(userUpdateInfo: UserUpdateInfo) = ???

}
