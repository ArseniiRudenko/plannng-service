package work.arudenko.kanban.backend.controller

import akka.http.scaladsl.server.Directives.{Authenticator, AuthenticatorPF}
import akka.http.scaladsl.server.directives.Credentials
import boopickle.{DecoderSpeed, Default, EncoderSpeed}
import boopickle.Default.Pickle
import com.redis.api.StringApi.Always
import com.typesafe.config.{Config, ConfigFactory}
import work.arudenko.kanban.backend.model.User
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.{Duration, FiniteDuration}


case class ValidAuth(token:String,user:User)

trait AuthenticatedRoute {

  import org.bouncycastle.crypto.generators.Argon2BytesGenerator
  import org.bouncycastle.crypto.params.Argon2Parameters
  import java.nio.charset.StandardCharsets
  import java.security.SecureRandom

  protected def generateArgon2id(password: String, salt: String): Array[Byte] =
    generateArgon2id(password,salt.asBase64)


  private val conf: Config = ConfigFactory.load()
  private val opsLimit: Int = conf.getInt("hash.opsLimit")
  private val memLimit: Int = conf.getInt("hash.memLimit")
  private val outputLength: Int = conf.getInt("hash.outputLength")
  private val parallelism: Int = conf.getInt("hash.parallelism")
  val authDuration: FiniteDuration = Duration.create(4, TimeUnit.HOURS)


  protected def generateArgon2id(password: String, salt: Array[Byte]): Array[Byte] = {

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

  protected def generateSalt: Array[Byte] = {
    val secureRandom = new SecureRandom
    val salt = new Array[Byte](128)
    secureRandom.nextBytes(salt)
    salt
  }


  import work.arudenko.kanban.backend.orm.RedisContext._
  import com.redis.serialization._
  import com.redis.serialization.Parse.Implicits._
  import boopickle.Default._

  protected def pickleState: PickleState = new PickleState(new EncoderSpeed)
  protected val unpickleState: ByteBuffer => Default.UnpickleState = (b: ByteBuffer) => new UnpickleState(new DecoderSpeed(b))
  protected implicit val userParser: Parse[User] = Parse(arr=>Unpickle[User].fromBytes(ByteBuffer.wrap(arr))(unpickleState))
  protected implicit val userSerializer:Format = Format({case u:User => Pickle.intoBytes(u)(pickleState,implicitly[Pickler[User]]).array()})

  val authenticator:Authenticator[ValidAuth] = {
    case Credentials.Missing => None
    case p:Credentials.Provided =>
      redis.withClient {
        client => client.get[User](p.identifier).map(u=>{
          client.expire(p.identifier,authDuration.toSeconds.toInt)
          ValidAuth(p.identifier,u)
        })
      }
  }



}
