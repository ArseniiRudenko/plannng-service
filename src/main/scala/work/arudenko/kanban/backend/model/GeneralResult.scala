package work.arudenko.kanban.backend.model
sealed trait Result[+T]

/**
 * @param code  for example: ''null''
 * @param message  for example: ''null''
*/
case class GeneralResult(
  code:Int, message: String
)extends Result[Nothing]

case object SuccessEmpty extends Result[Nothing]

case object NotFound extends Result[Nothing]
case object NotAuthorized extends Result[Nothing]
final case class WrongInput(message:String) extends Result[Nothing]
final case class SuccessEntity[T](value:T) extends Result[T]

