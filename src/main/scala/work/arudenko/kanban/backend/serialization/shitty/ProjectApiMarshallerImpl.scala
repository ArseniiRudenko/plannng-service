package work.arudenko.kanban.backend.serialization.shitty

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.api.ProjectApiMarshaller
import work.arudenko.kanban.backend.model.{Membership, MembershipInfo, Project, ProjectCreationInfo, UserInfo}

object ProjectApiMarshallerImpl extends ProjectApiMarshaller with JacksonMarshaller {
  override implicit val fromEntityUnmarshallerInt: FromEntityUnmarshaller[Int] = getUnmarshaller[Int]

  override implicit val fromEntityUnmarshallerProject: FromEntityUnmarshaller[Project] = getUnmarshaller[Project]

  override implicit val fromEntityUnmarshallerMembership: FromEntityUnmarshaller[Membership] = getUnmarshaller[Membership]

  override implicit val fromEntityUnmarshallerProjectCreationInfo: FromEntityUnmarshaller[ProjectCreationInfo] = getUnmarshaller[ProjectCreationInfo]

  override implicit val toEntityMarshallerProject: ToEntityMarshaller[Project] = getMarshaller[Project]

  override implicit val toEntityMarshallerMembershipInfo: ToEntityMarshaller[MembershipInfo] = getMarshaller[MembershipInfo]

  override implicit val toEntityMarshallerMembershipInfoSeq: ToEntityMarshaller[Seq[MembershipInfo]] = getMarshaller[Seq[MembershipInfo]]

  override implicit val toEntityMarshallerProjectSeq: ToEntityMarshaller[Seq[Project]] = getMarshaller[Seq[Project]]

  override implicit val fromEntityUnmarshallerUserInfo: FromEntityUnmarshaller[UserInfo] = getUnmarshaller[UserInfo]

  override implicit val fromEntityUnmarshallerMembershipInfo: FromEntityUnmarshaller[MembershipInfo] = getUnmarshaller[MembershipInfo]
}
