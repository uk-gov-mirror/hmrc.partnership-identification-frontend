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

package uk.gov.hmrc.partnershipidentificationfrontend.connectors

import play.api.libs.json.{Json, Reads, Writes}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.partnershipidentificationfrontend.config.AppConfig
import uk.gov.hmrc.partnershipidentificationfrontend.httpparsers.PartnershipIdentificationStorageHttpParser.{PartnershipIdentificationStorageHttpReads, SuccessfullyStored}
import uk.gov.hmrc.partnershipidentificationfrontend.httpparsers.RemovePartnershipDetailsHttpParser.{RemovePartnershipDetailsHttpReads, SuccessfullyRemoved}
import uk.gov.hmrc.partnershipidentificationfrontend.httpparsers.RetrievePartnershipFullJourneyDataHttpParser.RetrievePartnershipFullJourneyDataHttpReads
import uk.gov.hmrc.partnershipidentificationfrontend.models.{PartnershipFullJourneyData, PartnershipInformation}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PartnershipIdentificationConnector @Inject()(http: HttpClientV2,
                                                   appConfig: AppConfig
                                                  )(implicit ec: ExecutionContext) extends HttpReadsInstances {

  def retrievePartnershipInformation[DataType](journeyId: String,
                                               dataKey: String
                                              )(implicit dataTypeReads: Reads[DataType],
                                                manifest: Manifest[DataType],
                                                hc: HeaderCarrier): Future[Option[DataType]] =
    http.get(url = url"${appConfig.partnershipInformationUrl(journeyId)}/$dataKey")(hc).execute[Option[DataType]]


  def retrievePartnershipInformation(journeyId: String
                                    )(implicit hc: HeaderCarrier): Future[Option[PartnershipInformation]] =
    http.get(url = url"${appConfig.partnershipInformationUrl(journeyId)}")(hc).execute[Option[PartnershipInformation]]

  def retrievePartnershipFullJourneyData(journeyId: String
                                        )(implicit hc: HeaderCarrier): Future[Option[PartnershipFullJourneyData]] =
    http.get(url = url"${appConfig.partnershipInformationUrl(journeyId)}")(hc)
      .execute[Option[PartnershipFullJourneyData]](RetrievePartnershipFullJourneyDataHttpReads, ec)

  def storeData[DataType](journeyId: String, dataKey: String, data: DataType
                         )(implicit dataTypeWriter: Writes[DataType], hc: HeaderCarrier): Future[SuccessfullyStored.type] = {
    http.put(url = url"${appConfig.partnershipInformationUrl(journeyId)}/$dataKey")(hc).withBody(Json.toJson(data))
      .execute[SuccessfullyStored.type](PartnershipIdentificationStorageHttpReads, ec)
  }

  def removePartnershipInformation(journeyId: String,
                                   dataKey: String
                                  )(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    http.delete(url = url"${appConfig.partnershipInformationUrl(journeyId)}/$dataKey")(hc)
      .execute[SuccessfullyRemoved.type](RemovePartnershipDetailsHttpReads, ec)

}
