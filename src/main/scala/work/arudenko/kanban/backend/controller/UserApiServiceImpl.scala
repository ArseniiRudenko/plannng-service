package work.arudenko.kanban.backend.controller

import akka.http.javadsl.server.directives.SecurityDirectives.ProvidedCredentials
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import work.arudenko.kanban.backend.api.UserApiService
import work.arudenko.kanban.backend.model.{GeneralError, User}

class UserApiServiceImpl extends UserApiService{

  import org.bouncycastle.crypto.generators.Argon2BytesGenerator
  import org.bouncycastle.crypto.params.Argon2Parameters
  import java.nio.charset.StandardCharsets
  import java.security.SecureRandom
  import java.util.Base64

  def generateArgon2id(password: String, salt: String): Array[Byte] =
    generateArgon2id(password,base64Decoding(salt))

    def generateArgon2id(password: String, salt: Array[Byte]): Array[Byte] = {
      val opsLimit = 4
      val memLimit = 1048576
      val outputLength = 32
      val parallelism = 8
      val builder = new Argon2Parameters
        .Builder(Argon2Parameters.ARGON2_id)
        .withVersion(Argon2Parameters.ARGON2_VERSION_13)
        .withIterations(opsLimit)
        .withMemoryAsKB(memLimit)
        .withParallelism(parallelism)
        .withSalt(salt)
      val gen = new Argon2BytesGenerator
      gen.init(builder.build)
      val result = new Array[Byte](outputLength)
      gen.generateBytes(password.getBytes(StandardCharsets.UTF_8), result, 0, result.length)
      result
    }

    private def generateSalt = {
      val secureRandom = new SecureRandom
      val salt = new Array[Byte](32)
      secureRandom.nextBytes(salt)
      salt
    }

   private def base64Encoding(input: Array[Byte]) = Base64.getEncoder.encodeToString(input)
   private def base64Decoding(input: String) = Base64.getDecoder.decode(input)


  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def createUser(user: User)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def createUsersWithArrayInput(user: Seq[User])(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???

  /**
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 200, Message: Success
   */
  override def createUsersWithListInput(user: Seq[User])(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???

  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  override def deleteUser(username: String)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    User.getId(username) match {
      case Some(value) => User.delete(value); deleteUser200
      case None => deleteUser404
    }

  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  override def getUserByName(username: String)(implicit toEntityMarshallerUser: ToEntityMarshaller[User], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    User.getUserData(username) match {
      case Some(value) => getUserByName200(value)
      case None => getUserByName404
    }

  private def fakeCalculatingAndFuckOff(pw:String)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]):Route = {
    Credentials(Some(OAuth2BearerToken(pw)))
      .asInstanceOf[Credentials.Provided]
      .verify(null,pw=>base64Encoding(generateArgon2id(pw, "")))
    loginUser400(GeneralError(1,"wrong login or password"))
  }


  def generateSessionToken(user: User): String = ???

  /**
   * Code: 200, Message: successful operation, DataType: String
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def loginUser(username: String, password: String)(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route =
    User.getLoginUser(username) match {
      case Some(user) =>
        val creds = Credentials(Some(OAuth2BearerToken(password))).asInstanceOf[Credentials.Provided]
        user.password match {
          case Some(storedPassword) => {
            val (secret,salt) = storedPassword.splitAt(storedPassword.lastIndexOf(":"))
            val result = creds.verify(secret,curPw=>base64Encoding(generateArgon2id(curPw, salt)))
            if (result){
              loginUser200(generateSessionToken(user))
            }else {
              loginUser400(GeneralError(1,"wrong login or password"))
            }
          }
          case None => fakeCalculatingAndFuckOff(password)
        }
      case None => fakeCalculatingAndFuckOff(password)
    }
  /**
   * Code: 200, Message: Success
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   */
  override def logoutUser()(implicit toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???

  /**
   * Code: 200, Message: successful operation, DataType: User
   * Code: 400, Message: Invalid message format, DataType: GeneralError
   * Code: 404, Message: User not found
   */
  override def updateUser(username: String, user: User)(implicit toEntityMarshallerUser: ToEntityMarshaller[User], toEntityMarshallerGeneralError: ToEntityMarshaller[GeneralError]): Route = ???
}
