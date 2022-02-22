package work.arudenko.kanban.backend.controller

import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.server.Directives.{Authenticator, AuthenticatorPF}
import akka.http.scaladsl.server.directives.Credentials
import boopickle.{DecoderSpeed, Default, EncoderSpeed}
import boopickle.Default.Pickle
import com.redis.api.StringApi.Always
import com.typesafe.config.{Config, ConfigFactory}
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import work.arudenko.kanban.backend.model.User

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.{Duration, FiniteDuration}


case class Auth(token:String, user:User){

  def verifyPassword(password:String):Boolean =
    Auth.verifyPassword(password,user.password.get)
}

object Auth{

  def generateArgon2id(password: String, salt: String): Array[Byte] =
    generateArgon2id(password,salt.asBase64)


  private val conf: Config = ConfigFactory.load()
  val authDuration: FiniteDuration = Duration.create(4, TimeUnit.HOURS)
  private val opsLimit: Int = conf.getInt("hash.opsLimit")
  private val memLimit: Int = conf.getInt("hash.memLimit")
  private val outputLength: Int = conf.getInt("hash.outputLength")
  private val parallelism: Int = conf.getInt("hash.parallelism")
  private val saltLength: Int = conf.getInt("hash.saltLength")



  def generateArgon2id(password: String, salt: Array[Byte]): Array[Byte] = {

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

  def generateSalt: Array[Byte] = {
    val secureRandom = new SecureRandom
    val salt = new Array[Byte](saltLength)
    secureRandom.nextBytes(salt)
    salt
  }

  def hashPassword(plaintextPassword: String): String = {
    val salt: Array[Byte] = Auth.generateSalt
    val passHash = Auth.generateArgon2id(plaintextPassword, salt).toBase64
    s"$passHash:${salt.toBase64}"
  }

  def verifyPassword(password:String,storedPassword:String):Boolean = {
    val creds = Credentials(Some(OAuth2BearerToken(password))).asInstanceOf[Credentials.Provided]
    val pos=storedPassword.lastIndexOf(":")
    val (secret, salt) = (storedPassword.take(pos), storedPassword.drop(pos+1))
    creds.verify(secret, curPw => generateArgon2id(curPw, salt).toBase64)
  }

}
