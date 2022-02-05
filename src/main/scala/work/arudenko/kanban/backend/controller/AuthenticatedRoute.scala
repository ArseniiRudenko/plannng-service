package work.arudenko.kanban.backend.controller

import akka.http.scaladsl.server.Directives.{Authenticator, AuthenticatorPF}
import akka.http.scaladsl.server.directives.Credentials
import com.redis.api.StringApi.Always
import com.typesafe.config.{Config, ConfigFactory}
import work.arudenko.kanban.backend.model.User

import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.Duration


case class ValidAuth(token:String,user:User)

trait AuthenticatedRoute {

  import org.bouncycastle.crypto.generators.Argon2BytesGenerator
  import org.bouncycastle.crypto.params.Argon2Parameters
  import java.nio.charset.StandardCharsets
  import java.security.SecureRandom
  import java.util.Base64

  protected def generateArgon2id(password: String, salt: String): Array[Byte] =
    generateArgon2id(password,base64Decoding(salt))


  private val conf: Config = ConfigFactory.load()
  private val opsLimit: Int = conf.getInt("hash.opsLimit")
  private val memLimit: Int = conf.getInt("hash.memLimit")
  private val outputLength: Int = conf.getInt("hash.outputLength")
  private val parallelism: Int = conf.getInt("hash.parallelism")

  private def generateArgon2id(password: String, salt: Array[Byte]): Array[Byte] = {

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

  protected def generateSalt = {
    val secureRandom = new SecureRandom
    val salt = new Array[Byte](128)
    secureRandom.nextBytes(salt)
    salt
  }

  protected def base64Encoding(input: Array[Byte]) = Base64.getEncoder.encodeToString(input)
  protected def base64Decoding(input: String) = Base64.getDecoder.decode(input)

  import work.arudenko.kanban.backend.orm.RedisContext._
  import com.redis.serialization._
  import com.redis.serialization.Parse.Implicits._
  import boopickle.Default._

  implicit val userParser: Parse[User] = Parse(arr=>Unpickle[User].fromBytes(ByteBuffer.wrap(arr)))

  val authenticator:Authenticator[ValidAuth] = {
    case Credentials.Missing => None
    case p:Credentials.Provided => {
      redis.withClient {
        client => client.get[User](p.identifier).map(u=>ValidAuth(p.identifier,u))
      }
    }
  }


}
