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

package uk.gov.hmrc.partnershipidentificationfrontend.service

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.partnershipidentificationfrontend.connectors.PartnershipIdentificationConnector
import uk.gov.hmrc.partnershipidentificationfrontend.httpparsers.PartnershipIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.partnershipidentificationfrontend.httpparsers.RemovePartnershipDetailsHttpParser.SuccessfullyRemoved
import uk.gov.hmrc.partnershipidentificationfrontend.models.{BusinessVerificationStatus, CompanyProfile, PartnershipFullJourneyData, PartnershipInformation}
import uk.gov.hmrc.partnershipidentificationfrontend.models.{RegistrationStatus, ValidationResponse}
 

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PartnershipIdentificationService @Inject()(connector: PartnershipIdentificationConnector)(implicit ec: ExecutionContext) {

  def storeSautr(journeyId: String,
                 sautr: String
                )(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[String](journeyId, PartnershipIdentificationService.SautrKey, sautr)

  def storePostCode(journeyId: String,
                    postCode: String
                   )(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[String](journeyId, PartnershipIdentificationService.PostCodeKey, postCode)

  def storeBusinessVerificationStatus(journeyId: String,
                                      businessVerification: BusinessVerificationStatus
                                     )(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[BusinessVerificationStatus](journeyId, PartnershipIdentificationService.VerificationStatusKey, businessVerification)

  def storeIdentifiersMatch(journeyId: String,
                            identifiersMatch: ValidationResponse
                           )(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[ValidationResponse](journeyId, PartnershipIdentificationService.IdentifiersMatchKey, identifiersMatch)

  def storeRegistrationStatus(journeyId: String,
                              registrationStatus: RegistrationStatus
                             )(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[RegistrationStatus](journeyId, PartnershipIdentificationService.RegistrationKey, registrationStatus)(RegistrationStatus.format, hc)

  def retrieveSautr(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrievePartnershipInformation[String](journeyId, PartnershipIdentificationService.SautrKey)

  def retrievePostCode(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrievePartnershipInformation[String](journeyId, PartnershipIdentificationService.PostCodeKey)

  def removeSaInformation(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] = for {
    _ <- connector.removePartnershipInformation(journeyId, PartnershipIdentificationService.SautrKey)
    _ <- connector.removePartnershipInformation(journeyId, PartnershipIdentificationService.PostCodeKey)
  } yield SuccessfullyRemoved

  def retrievePartnershipInformation(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[PartnershipInformation]] =
    connector.retrievePartnershipInformation(journeyId)

  def retrievePartnershipFullJourneyData(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[PartnershipFullJourneyData]] =
    connector.retrievePartnershipFullJourneyData(journeyId)

  def retrieveBusinessVerificationStatus(journeyId: String
                                        )(implicit hc: HeaderCarrier): Future[Option[BusinessVerificationStatus]] =
    connector.retrievePartnershipInformation[BusinessVerificationStatus](journeyId, PartnershipIdentificationService.VerificationStatusKey)

  def retrieveCompanyProfile(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[CompanyProfile]] =
    connector.retrievePartnershipInformation[CompanyProfile](journeyId, PartnershipIdentificationService.CompanyProfileKey)

}

object PartnershipIdentificationService {
  val SautrKey: String = "sautr"
  val PostCodeKey: String = "postcode"
  val VerificationStatusKey: String = "businessVerification"
  val IdentifiersMatchKey: String = "identifiersMatch"
  val RegistrationKey: String = "registration"
  val CompanyProfileKey: String = "companyProfile"
}