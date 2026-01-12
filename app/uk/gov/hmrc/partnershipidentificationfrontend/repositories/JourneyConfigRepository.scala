/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.partnershipidentificationfrontend.repositories

import play.api.libs.json._
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions}
import org.mongodb.scala.result.{DeleteResult, InsertOneResult}
import uk.gov.hmrc.partnershipidentificationfrontend.config.AppConfig
import uk.gov.hmrc.partnershipidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.partnershipidentificationfrontend.models.PartnershipType._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyConfigRepository @Inject()(mongoComponent: MongoComponent,
                                        appConfig: AppConfig)
                                       (implicit ec: ExecutionContext) extends PlayMongoRepository[JsObject](
  collectionName = "partnership-identification-frontend",
  mongoComponent = mongoComponent,
  domainFormat = implicitly[Format[JsObject]],
  indexes = scala.Seq(JourneyConfigRepository.timeToLiveIndex(appConfig.timeToLiveSeconds)),
  extraCodecs = scala.Seq(Codecs.playFormatCodec(JourneyConfigRepository.journeyConfigMongoFormat))
){

  def insertJourneyConfig(journeyId: String, authInternalId: String, journeyConfig: JourneyConfig): Future[InsertOneResult] = {

    val document: JsObject = Json.obj(
      JourneyConfigRepository.JourneyIdKey -> journeyId,
      JourneyConfigRepository.AuthInternalIdKey -> authInternalId,
      JourneyConfigRepository.CreationTimestampKey -> Json.obj("$date" -> Instant.now.toEpochMilli)
    ) ++ Json.toJsObject(journeyConfig)

    collection.insertOne(document).toFuture()
  }

  def findJourneyConfig(journeyId: String, authInternalId: String): Future[Option[JourneyConfig]] =
    collection
      .find[JsObject](
        Filters.and(
          Filters.equal(JourneyConfigRepository.JourneyIdKey, journeyId),
          Filters.equal(JourneyConfigRepository.AuthInternalIdKey, authInternalId)
        )
      )
      .headOption()
      .map(_.flatMap(js => js.asOpt[JourneyConfig](JourneyConfig.format)))

  def count: Future[Long] = collection.countDocuments().toFuture()

  def removeJourneyConfig(journeyId: String, authInternalId: String): Future[DeleteResult] = {

    collection.deleteOne(
      Filters.and(
        Filters.equal(JourneyConfigRepository.JourneyIdKey, journeyId),
        Filters.equal(JourneyConfigRepository.AuthInternalIdKey, authInternalId)
      )
    ).toFuture()

  }

  def drop: Future[Unit] = collection.drop().toFuture().map(_ => ())

}

object JourneyConfigRepository {
  val JourneyIdKey = "_id"
  val AuthInternalIdKey = "authInternalId"
  val CreationTimestampKey = "creationTimestamp"
  val GeneralPartnershipKey = "generalPartnership"
  val ScottishPartnershipKey = "scottishPartnership"
  val ScottishLimitedPartnershipKey = "scottishLimitedPartnership"
  val LimitedPartnershipKey = "limitedPartnership"
  val LimitedLiabilityPartnershipKey = "limitedLiabilityPartnership"

  def timeToLiveIndex(timeToLiveDuration: Long): IndexModel = {
    IndexModel(
      keys = ascending(CreationTimestampKey),
      indexOptions = IndexOptions()
        .name("PartnershipInformationExpires")
        .expireAfter(timeToLiveDuration, TimeUnit.SECONDS)
    )
  }

  implicit val partnershipTypeMongoFormat: Format[PartnershipType] = new Format[PartnershipType] {
    override def reads(json: JsValue): JsResult[PartnershipType] = json.validate[String].collect(JsonValidationError("Invalid partnership type")) {
      case GeneralPartnershipKey => GeneralPartnership
      case ScottishPartnershipKey => ScottishPartnership
      case ScottishLimitedPartnershipKey => ScottishLimitedPartnership
      case LimitedPartnershipKey => LimitedPartnership
      case LimitedLiabilityPartnershipKey => LimitedLiabilityPartnership
    }

    override def writes(partnershipType: PartnershipType): JsValue = partnershipType match {
      case GeneralPartnership => JsString(GeneralPartnershipKey)
      case ScottishPartnership => JsString(ScottishPartnershipKey)
      case ScottishLimitedPartnership => JsString(ScottishLimitedPartnershipKey)
      case LimitedPartnership => JsString(LimitedPartnershipKey)
      case LimitedLiabilityPartnership => JsString(LimitedLiabilityPartnershipKey)
    }
  }
  implicit val journeyConfigMongoFormat: OFormat[JourneyConfig] = Json.format[JourneyConfig]
}