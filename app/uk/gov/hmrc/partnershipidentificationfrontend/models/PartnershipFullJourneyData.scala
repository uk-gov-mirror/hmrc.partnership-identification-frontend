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

package uk.gov.hmrc.partnershipidentificationfrontend.models

import play.api.libs.functional.syntax._
import play.api.libs.json._

import uk.gov.hmrc.partnershipidentificationfrontend.models.BusinessVerificationStatus._
import uk.gov.hmrc.partnershipidentificationfrontend.models.ValidationResponse.IdentifiersMatchedKey

case class PartnershipFullJourneyData(optPostcode: Option[String],
                                      optSautr: Option[String],
                                      companyProfile: Option[CompanyProfile],
                                      identifiersMatch: ValidationResponse,
                                      businessVerification: Option[BusinessVerificationStatus],
                                      registrationStatus: RegistrationStatus
                                     )

object PartnershipFullJourneyData {

  private val SautrKey = "sautr"
  private val PostcodeKey = "postcode"
  private val IdentifiersMatchKey = "identifiersMatch"
  private val BusinessVerificationKey = "businessVerification"
  private val RegistrationStatusKey = "registration"
  private val CompanyProfileKey = "companyProfile"

  private val BusinessVerificationUnchallengedKey = "UNCHALLENGED"

  val reads: Reads[PartnershipFullJourneyData] = (
    (JsPath \ PostcodeKey).readNullable[String] and
      (JsPath \ SautrKey).readNullable[String] and
      (JsPath \ CompanyProfileKey).readNullable[CompanyProfile] and
      (JsPath \ IdentifiersMatchKey).read[ValidationResponse] and
      (JsPath \ BusinessVerificationKey).readNullable[BusinessVerificationStatus] and
      (JsPath \ RegistrationStatusKey).read[RegistrationStatus](RegistrationStatus.format)
    ) (PartnershipFullJourneyData.apply _)

  val writes: OWrites[PartnershipFullJourneyData] = (
    (JsPath \ PostcodeKey).writeNullable[String] and
      (JsPath \ SautrKey).writeNullable[String] and
      (JsPath \ CompanyProfileKey).writeNullable[CompanyProfile] and
      (JsPath \ IdentifiersMatchKey).write[ValidationResponse] and
      (JsPath \ BusinessVerificationKey).writeNullable[BusinessVerificationStatus] and
      (JsPath \ RegistrationStatusKey).write[RegistrationStatus](RegistrationStatus.format)
    ) (o => Tuple.fromProductTyped(o))

  implicit val format: OFormat[PartnershipFullJourneyData] = OFormat(reads, writes)

  val jsonWriterForCallingServices: Writes[PartnershipFullJourneyData] =
    (partnershipFullJourneyData: PartnershipFullJourneyData) =>
      format.writes(partnershipFullJourneyData) ++
        newIdentifiersBlock(partnershipFullJourneyData) ++
        newBusinessVerificationBlock(partnershipFullJourneyData)

  private def newIdentifiersBlock(partnershipFullJourneyData: PartnershipFullJourneyData): JsObject =
    Json.obj(IdentifiersMatchKey -> partnershipFullJourneyData.identifiersMatch.toString.contains(IdentifiersMatchedKey))

  private def newBusinessVerificationBlock(partnershipFullJourneyData: PartnershipFullJourneyData): JsObject =
    partnershipFullJourneyData.businessVerification.map(businessVerification => {
      val businessVerificationStatusForCallingServices: String = businessVerification match {
        case BusinessVerificationNotEnoughInformationToCallBV |
             BusinessVerificationNotEnoughInformationToChallenge => BusinessVerificationUnchallengedKey
        case BusinessVerificationPass => BusinessVerificationPassKey
        case BusinessVerificationFail => BusinessVerificationFailKey
      }
      Json.obj(BusinessVerificationKey -> Json.obj(BusinessVerificationStatusKey -> businessVerificationStatusForCallingServices))
    })
      .getOrElse(Json.obj())
}
