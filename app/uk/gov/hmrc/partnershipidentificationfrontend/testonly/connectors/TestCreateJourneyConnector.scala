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

package uk.gov.hmrc.partnershipidentificationfrontend.testonly.connectors

import play.api.http.Status.CREATED
import play.api.libs.json.{JsObject, Json, Writes}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.mvc.Call
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, InternalServerException, StringContextOps}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.partnershipidentificationfrontend.api.controllers.{JourneyController, routes}
import uk.gov.hmrc.partnershipidentificationfrontend.config.AppConfig
import uk.gov.hmrc.partnershipidentificationfrontend.models.{JourneyConfig, JourneyLabels}
import uk.gov.hmrc.partnershipidentificationfrontend.testonly.connectors.TestCreateJourneyConnector.journeyConfigWriter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestCreateJourneyConnector @Inject()(httpClient: HttpClientV2,
                                           appConfig: AppConfig
                                          )(implicit ec: ExecutionContext) {

  def createGeneralPartnershipJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[String] =
    postTo(destination = routes.JourneyController.createGeneralPartnershipJourney(), journeyConfig = journeyConfig)

  def createScottishPartnershipJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[String] =
    postTo(destination = routes.JourneyController.createScottishPartnershipJourney(), journeyConfig = journeyConfig)

  def createScottishLimitedPartnershipJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[String] =
    postTo(destination = routes.JourneyController.createScottishLimitedPartnershipJourney(), journeyConfig = journeyConfig)

  def createLimitedPartnershipJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[String] =
    postTo(destination = routes.JourneyController.createLimitedPartnershipJourney(), journeyConfig = journeyConfig)

  def createLimitedLiabilityPartnershipJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[String] =
    postTo(destination = routes.JourneyController.createLimitedLiabilityPartnershipJourney(), journeyConfig = journeyConfig)

  private def postTo(destination: Call, journeyConfig: JourneyConfig)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[String] = {
    val url = appConfig.selfBaseUrl + destination.url

    httpClient.post(url"$url").withBody(Json.toJson(journeyConfig)).execute[HttpResponse].map {
      case response@HttpResponse(CREATED, _, _) =>
        (response.json \ "journeyStartUrl").as[String]
      case response =>
        throw new InternalServerException(s"Invalid response from Destination : ${destination.url}  Status : ${response.status} Body : ${response.body}")
    }
  }

}

object TestCreateJourneyConnector {
  implicit val journeyConfigWriter: Writes[JourneyConfig] = (journeyConfig: JourneyConfig) => Json.obj(
    JourneyController.continueUrlKey -> journeyConfig.continueUrl,
    JourneyController.businessVerificationCheckKey -> journeyConfig.businessVerificationCheck,
    JourneyController.deskProServiceIdKey -> journeyConfig.pageConfig.deskProServiceId,
    JourneyController.signOutUrlKey -> journeyConfig.pageConfig.signOutUrl,
    JourneyController.accessibilityUrlKey -> journeyConfig.pageConfig.accessibilityUrl,
    JourneyController.regimeKey -> journeyConfig.regime
  ) ++ labelsAsOptJsObject(journeyConfig.pageConfig.optLabels)

  private def labelsAsOptJsObject(optJourneyLabels: Option[JourneyLabels]): JsObject =
    optJourneyLabels match {
      case Some(journeyLabels) => Json.obj(JourneyController.labelsKey -> Json.toJsObject(journeyLabels))
      case _ => Json.obj()
    }
}
