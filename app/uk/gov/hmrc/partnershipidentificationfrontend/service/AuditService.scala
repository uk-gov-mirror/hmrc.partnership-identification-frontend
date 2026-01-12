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

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.partnershipidentificationfrontend.config.AppConfig
import uk.gov.hmrc.partnershipidentificationfrontend.models.PartnershipType._
import uk.gov.hmrc.partnershipidentificationfrontend.models._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuditService @Inject()(auditConnector: AuditConnector,
                             partnershipIdentificationService: PartnershipIdentificationService,
                             appConfig: AppConfig)(implicit ec: ExecutionContext) {
  def auditPartnershipInformation(journeyId: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[Unit] = {
    for {
      partnershipData <- partnershipIdentificationService.retrievePartnershipFullJourneyData(journeyId)
    } yield partnershipData match {
      case Some(PartnershipFullJourneyData(optPostcode, optSautr, optCompanyProfile, identifiersMatch, businessVerification, registrationStatus)) =>
        val auditJson = (sautrJson(optSautr)
          ++ postcodeJson(optPostcode)
          ++ identifiersMatchJson(identifiersMatch)
          ++ businessTypeJson(journeyConfig.partnershipType)
          ++ verificationStatusJson(businessVerification)
          ++ registerApiStatusJson(registrationStatus)
          ++ callingServiceJson(journeyConfig.pageConfig.optServiceName)
          ++ companyNumberJson(optCompanyProfile))

        auditConnector.sendExplicitAudit(auditType(journeyConfig.partnershipType), auditJson)
      case None =>
    }
  }

  private def auditType(partnershipType: PartnershipType): String = partnershipType match {
    case GeneralPartnership => "GeneralPartnershipEntityRegistration"
    case ScottishPartnership => "ScottishPartnershipEntityRegistration"
    case LimitedPartnership => "LimitedPartnershipRegistration"
    case LimitedLiabilityPartnership => "LimitedLiabilityPartnershipRegistration"
    case ScottishLimitedPartnership => "ScottishLTDPartnershipRegistration"
  }

  private def companyNumberJson(optCompanyProfile: Option[CompanyProfile]): JsObject = optCompanyProfile match {
    case Some(companyProfile) => Json.obj(
      "companyNumber" -> companyProfile.companyNumber
    )
    case None => Json.obj()
  }

  private def sautrJson(optSautr: Option[String]): JsObject = optSautr match {
    case Some(sautr) => Json.obj(
      "SAUTR" -> sautr
    )
    case None => Json.obj()
  }

  private def postcodeJson(optPostcode: Option[String]): JsObject = optPostcode match {
    case Some(postcode) => Json.obj(
      "SApostcode" -> postcode
    )
    case None => Json.obj()
  }

  private def businessTypeJson(partnershipType: PartnershipType): JsObject = {
    val businessType = partnershipType match {
      case GeneralPartnership => "General Partnership"
      case ScottishPartnership => "Scottish Partnership"
      case LimitedPartnership => "Limited Partnership"
      case LimitedLiabilityPartnership => "Limited Liability Partnership"
      case ScottishLimitedPartnership => "Scottish LTD Partnership"
    }
    Json.obj(
      "businessType" -> businessType
    )
  }

  private def verificationStatusJson(businessVerification: Option[BusinessVerificationStatus]): JsObject =
    Json.obj(
      "VerificationStatus" -> (businessVerification match {
        case Some(BusinessVerificationPass) =>
          "success"
        case Some(BusinessVerificationFail) =>
          "fail"
        case Some(BusinessVerificationNotEnoughInformationToChallenge) =>
          "Not Enough Information to challenge"
        case Some(BusinessVerificationNotEnoughInformationToCallBV) =>
          "Not Enough Information to call BV"
        case None =>
          "not requested"
      })
    )

  private def registerApiStatusJson(registrationStatus: RegistrationStatus): JsObject = Json.obj(
    "RegisterApiStatus" -> (registrationStatus match {
      case Registered(_) =>
        "success"
      case RegistrationFailed(_) =>
        "fail"
      case RegistrationNotCalled =>
        "not called"
    })
  )

  private def callingServiceJson(optServiceName: Option[String]): JsObject = {
    val serviceName = optServiceName.getOrElse(appConfig.defaultServiceName)
    Json.obj(
      "callingService" -> serviceName
    )
  }

  private def identifiersMatchJson(identifiersMatch: ValidationResponse): JsObject = {

    val matchStatus: String = identifiersMatch match {
      case IdentifiersMatched => "true"
      case IdentifiersMismatch => "false"
      case UnMatchable => "unmatchable"
    }

    Json.obj {
      "isMatch" -> matchStatus
    }
  }
}
