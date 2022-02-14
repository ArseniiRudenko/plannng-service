package work.arudenko.kanban.backend.services

import work.arudenko.kanban.backend.model.User

object EmailService {

  def sendActivaltionEmail(user:User,activationToken:String):Unit = ???

  def sendPasswordResetEmail(user:User,pwResetToken:String):Unit = ???

}
