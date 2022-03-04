package work.arudenko.kanban.backend.serialization.shitty

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import work.arudenko.kanban.backend.api.ProjectApiMarshaller
import work.arudenko.kanban.backend.model.{Membership, MembershipInfo, Project, ProjectCreationInfo}

object ProjectApiMarshallerImpl extends ProjectApiMarshaller with JacksonMarshaller {
  override implicit def fromEntityUnmarshallerInt: FromEntityUnmarshaller[Int] = getUnmarshaller[Int]

  override implicit def fromEntityUnmarshallerProject: FromEntityUnmarshaller[Project] = getUnmarshaller[Project]

  override implicit def fromEntityUnmarshallerMembership: FromEntityUnmarshaller[Membership] = getUnmarshaller[Membership]

  override implicit def fromEntityUnmarshallerProjectCreationInfo: FromEntityUnmarshaller[ProjectCreationInfo] = getUnmarshaller[ProjectCreationInfo]

  override implicit def toEntityMarshallerProject: ToEntityMarshaller[Project] = getMarshaller[Project]

  override implicit def toEntityMarshallerMembershipInfo: ToEntityMarshaller[MembershipInfo] = getMarshaller[MembershipInfo]

  override implicit def toEntityMarshallerMembershipInfoSeq: ToEntityMarshaller[Seq[MembershipInfo]] = getMarshaller[Seq[MembershipInfo]]

  override implicit def toEntityMarshallerProjectSeq: ToEntityMarshaller[Seq[Project]] = getMarshaller[Seq[Project]]
}
