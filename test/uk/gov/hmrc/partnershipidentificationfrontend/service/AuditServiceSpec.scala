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

import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.{verify, when}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.partnershipidentificationfrontend.config.AppConfig
import uk.gov.hmrc.partnershipidentificationfrontend.helpers.TestConstants._
import uk.gov.hmrc.partnershipidentificationfrontend.models._
import uk.gov.hmrc.partnershipidentificationfrontend.models.PartnershipType._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {

  trait Setup {

    val limitedPartnershipFullJourneyData: PartnershipFullJourneyData = PartnershipFullJourneyData(
      optPostcode = Some(testPostcode),
      optSautr = Some(testSautr),
      companyProfile = Some(testCompanyProfile),
      identifiersMatch = IdentifiersMismatch,
      businessVerification = Some(BusinessVerificationNotEnoughInformationToCallBV),
      registrationStatus = RegistrationNotCalled
    )

    val defaultScottishPartnershipJourneyConfig: JourneyConfig = testDefaultGeneralPartnershipJourneyConfig.copy(partnershipType = ScottishPartnership)
    val mockAuditConnector: AuditConnector = mock[AuditConnector]
    val mockPartnershipIdentificationService: PartnershipIdentificationService = mock[PartnershipIdentificationService]
    val mockAppConfig: AppConfig = mock[AppConfig]

    implicit val hc: HeaderCarrier = HeaderCarrier()

    object TestAuditService extends AuditService(
      mockAuditConnector,
      mockPartnershipIdentificationService,
      mockAppConfig
    )

  }

  "auditPartnershipInformation" when {
    "the user is identifying a general partnership with all information" when {
      "the user has successfully passed business verification" should {
        "audit the correct information" in new Setup {
          when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
            PartnershipFullJourneyData(
              optPostcode = Some(testPostcode),
              optSautr = Some(testSautr),
              companyProfile = None,
              identifiersMatch = IdentifiersMatched,
              businessVerification = Some(BusinessVerificationPass),
              registrationStatus = Registered(testBusinessPartnerId)
            )
          )))

          val expectedAuditModel: JsObject = expectedNonLimitedPartnershipAuditJson("General Partnership", "success", "success")

          await(TestAuditService.auditPartnershipInformation(
            testJourneyId,
            testDefaultGeneralPartnershipJourneyConfig,
          ))

          verify(mockAuditConnector).sendExplicitAudit("GeneralPartnershipEntityRegistration", expectedAuditModel)
        }
      }
      "business verification could not find a record for the user" should {
        "audit the correct information" in new Setup {
          when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
            PartnershipFullJourneyData(
              optPostcode = Some(testPostcode),
              optSautr = Some(testSautr),
              companyProfile = None,
              identifiersMatch = IdentifiersMatched,
              businessVerification = Some(BusinessVerificationNotEnoughInformationToChallenge),
              registrationStatus = RegistrationNotCalled
            )
          )))

          val expectedAuditModel: JsObject = expectedNonLimitedPartnershipAuditJson("General Partnership", "Not Enough Information to challenge", "not called")

          await(TestAuditService.auditPartnershipInformation(
            testJourneyId,
            testDefaultGeneralPartnershipJourneyConfig
          ))
          verify(mockAuditConnector).sendExplicitAudit("GeneralPartnershipEntityRegistration", expectedAuditModel)
        }
      }
      "the user has failed business verification" should {
        "audit the correct information" in new Setup {
          when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
            PartnershipFullJourneyData(
              optPostcode = Some(testPostcode),
              optSautr = Some(testSautr),
              companyProfile = None,
              identifiersMatch = IdentifiersMatched,
              businessVerification = Some(BusinessVerificationFail),
              registrationStatus = RegistrationNotCalled
            )
          )))

          val expectedAuditModel: JsObject = expectedNonLimitedPartnershipAuditJson("General Partnership", "fail", "not called")

          await(TestAuditService.auditPartnershipInformation(
            testJourneyId,
            testDefaultGeneralPartnershipJourneyConfig
          ))
          verify(mockAuditConnector).sendExplicitAudit("GeneralPartnershipEntityRegistration", expectedAuditModel)
        }
      }
      "the user has passed business verification but the registration call failed" should {
        "audit the correct information" in new Setup {
          when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
            PartnershipFullJourneyData(
              optPostcode = Some(testPostcode),
              optSautr = Some(testSautr),
              companyProfile = None,
              identifiersMatch = IdentifiersMatched,
              businessVerification = Some(BusinessVerificationPass),
              registrationStatus = testRegistrationFailedWithSingleFailure
            )
          )))

          val expectedAuditModel: JsObject = expectedNonLimitedPartnershipAuditJson("General Partnership", "success", "fail")

          await(TestAuditService.auditPartnershipInformation(
            testJourneyId,
            testDefaultGeneralPartnershipJourneyConfig
          ))
          verify(mockAuditConnector).sendExplicitAudit("GeneralPartnershipEntityRegistration", expectedAuditModel)
        }
      }
      "there is no business verification status" should {
        "audit the correct information" in new Setup {
          when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
            PartnershipFullJourneyData(
              optPostcode = Some(testPostcode),
              optSautr = Some(testSautr),
              companyProfile = None,
              identifiersMatch = IdentifiersMatched,
              businessVerification = None,
              registrationStatus = testRegistrationFailedWithSingleFailure
            )
          )))

          val expectedAuditModel: JsObject = expectedNonLimitedPartnershipAuditJson("General Partnership", "not requested", "fail")

          await(TestAuditService.auditPartnershipInformation(
            testJourneyId,
            testDefaultGeneralPartnershipJourneyConfig
          ))
          verify(mockAuditConnector).sendExplicitAudit("GeneralPartnershipEntityRegistration", expectedAuditModel)
        }
      }
    }
    "the user is identifying a general partnership with no SAUTR and the calling service has not provided a service name" should {
      "audit the correct information" in new Setup {
        when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
          PartnershipFullJourneyData(
            optPostcode = None,
            optSautr = None,
            companyProfile = None,
            identifiersMatch = UnMatchable,
            businessVerification = Some(BusinessVerificationNotEnoughInformationToCallBV),
            registrationStatus = RegistrationNotCalled
          )
        )))

        when(mockAppConfig.defaultServiceName).thenReturn(testServiceName)

        val expectedAuditModel: JsObject = Json.obj(
          "isMatch" -> "unmatchable",
          "businessType" -> "General Partnership",
          "VerificationStatus" -> "Not Enough Information to call BV",
          "RegisterApiStatus" -> "not called",
          "callingService" -> testServiceName
        )

        await(TestAuditService.auditPartnershipInformation(
          testJourneyId,
          testDefaultGeneralPartnershipJourneyConfig.copy(pageConfig = testDefaultPageConfig.copy(optServiceName = None))
        ))
        verify(mockAuditConnector).sendExplicitAudit("GeneralPartnershipEntityRegistration", expectedAuditModel)
      }
    }
    "the user is identifying a scottish partnership with all information" when {
      "the user has successfully passed business verification" should {
        "audit the correct information" in new Setup {
          when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
            PartnershipFullJourneyData(
              optPostcode = Some(testPostcode),
              optSautr = Some(testSautr),
              companyProfile = None,
              identifiersMatch = IdentifiersMatched,
              businessVerification = Some(BusinessVerificationPass),
              registrationStatus = Registered(testBusinessPartnerId)
            )
          )))

          val expectedAuditModel: JsObject = expectedNonLimitedPartnershipAuditJson("Scottish Partnership", "success", "success")

          await(TestAuditService.auditPartnershipInformation(
            testJourneyId,
            testDefaultGeneralPartnershipJourneyConfig.copy(partnershipType = ScottishPartnership)
          ))

          verify(mockAuditConnector).sendExplicitAudit("ScottishPartnershipEntityRegistration", expectedAuditModel)
        }
      }
      "the user has failed business verification" should {
        "audit the correct information" in new Setup {
          when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
            PartnershipFullJourneyData(
              optPostcode = Some(testPostcode),
              optSautr = Some(testSautr),
              companyProfile = None,
              identifiersMatch = IdentifiersMatched,
              businessVerification = Some(BusinessVerificationFail),
              registrationStatus = RegistrationNotCalled
            )
          )))

          val expectedAuditModel: JsObject = expectedNonLimitedPartnershipAuditJson("Scottish Partnership", "fail", "not called")

          await(TestAuditService.auditPartnershipInformation(
            testJourneyId,
            defaultScottishPartnershipJourneyConfig
          ))
          verify(mockAuditConnector).sendExplicitAudit("ScottishPartnershipEntityRegistration", expectedAuditModel)
        }
      }
      "the user has passed business verification but the registration call failed" should {
        "audit the correct information" in new Setup {
          when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
            PartnershipFullJourneyData(
              optPostcode = Some(testPostcode),
              optSautr = Some(testSautr),
              companyProfile = None,
              identifiersMatch = IdentifiersMatched,
              businessVerification = Some(BusinessVerificationPass),
              registrationStatus = testRegistrationFailedWithSingleFailure
            )
          )))

          val expectedAuditModel: JsObject = expectedNonLimitedPartnershipAuditJson("Scottish Partnership", "success", "fail")

          await(TestAuditService.auditPartnershipInformation(
            testJourneyId,
            defaultScottishPartnershipJourneyConfig,
          ))
          verify(mockAuditConnector).sendExplicitAudit("ScottishPartnershipEntityRegistration", expectedAuditModel)
        }
      }
      "there is no business verification status" should {
        "audit the correct information" in new Setup {
          when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
            PartnershipFullJourneyData(
              optPostcode = Some(testPostcode),
              optSautr = Some(testSautr),
              companyProfile = None,
              identifiersMatch = IdentifiersMatched,
              businessVerification = None,
              registrationStatus = testRegistrationFailedWithSingleFailure
            )
          )))

          val expectedAuditModel: JsObject = expectedNonLimitedPartnershipAuditJson("Scottish Partnership", "not requested", "fail")

          await(TestAuditService.auditPartnershipInformation(
            testJourneyId,
            defaultScottishPartnershipJourneyConfig
          ))
          verify(mockAuditConnector).sendExplicitAudit("ScottishPartnershipEntityRegistration", expectedAuditModel)
        }
      }
    }
    "the user is identifying a scottish partnership with no SAUTR and the calling service has not provided a service name" should {
      "audit the correct information" in new Setup {
        when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
          PartnershipFullJourneyData(
            optPostcode = None,
            optSautr = None,
            companyProfile = None,
            identifiersMatch = UnMatchable,
            businessVerification = Some(BusinessVerificationNotEnoughInformationToCallBV),
            registrationStatus = RegistrationNotCalled
          )
        )))

        when(mockAppConfig.defaultServiceName).thenReturn(testServiceName)

        val expectedAuditModel: JsObject = Json.obj(
          "isMatch" -> "unmatchable",
          "businessType" -> "Scottish Partnership",
          "VerificationStatus" -> "Not Enough Information to call BV",
          "RegisterApiStatus" -> "not called",
          "callingService" -> testServiceName
        )

        await(TestAuditService.auditPartnershipInformation(
          testJourneyId,
          defaultScottishPartnershipJourneyConfig.copy(pageConfig = testDefaultPageConfig.copy(optServiceName = None))
        ))
        verify(mockAuditConnector).sendExplicitAudit("ScottishPartnershipEntityRegistration", expectedAuditModel)
      }
    }

    "the user is identifying a limited partnership with all information" when {

      val journeyConfig = testDefaultGeneralPartnershipJourneyConfig.copy(partnershipType = LimitedPartnership)

      val expectedAuditModel: JsObject = expectedLimitedPartnershipAuditJson( partnershipType = "Limited Partnership")

      "identifiersMatch is false" should {
        "audit the correct information" in new Setup {
          when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
            limitedPartnershipFullJourneyData
          )))

          await(
            TestAuditService.auditPartnershipInformation(
              journeyId = testJourneyId,
              journeyConfig = journeyConfig
            )
          )
          verify(mockAuditConnector).sendExplicitAudit(auditType = "LimitedPartnershipRegistration", detail = expectedAuditModel)
        }
      }
      "identifiersMatch is false and service name has not provided" should {
        "audit the correct information" in new Setup {
          when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
            limitedPartnershipFullJourneyData
          )))

          when(mockAppConfig.defaultServiceName).thenReturn(testServiceName)

          await(TestAuditService.auditPartnershipInformation(
            journeyId = testJourneyId,
            journeyConfig = journeyConfig.copy(pageConfig = testDefaultPageConfig.copy(optServiceName = None))
          ))
          verify(mockAuditConnector).sendExplicitAudit("LimitedPartnershipRegistration", expectedAuditModel)
        }
      }
    }

    "the user is identifying a limited liability partnership with all information" when {

      val journeyConfig = testDefaultGeneralPartnershipJourneyConfig.copy(partnershipType = LimitedLiabilityPartnership)

      val expectedAuditModel: JsObject = expectedLimitedPartnershipAuditJson( partnershipType = "Limited Liability Partnership")

      "identifiersMatch is false" should {
        "audit the correct information" in new Setup {
          when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
            limitedPartnershipFullJourneyData
          )))

          await(TestAuditService.auditPartnershipInformation(
            journeyId = testJourneyId,
            journeyConfig = journeyConfig
          ))

          verify(mockAuditConnector).sendExplicitAudit(
            auditType = "LimitedLiabilityPartnershipRegistration",
            detail = expectedAuditModel
          )
        }
      }

      "identifiersMatch is false and service name has not provided" should {
        "audit the correct information" in new Setup {
          when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
            limitedPartnershipFullJourneyData
          )))

          when(mockAppConfig.defaultServiceName).thenReturn(testServiceName)

          await(TestAuditService.auditPartnershipInformation(
            journeyId = testJourneyId,
            journeyConfig = journeyConfig.copy(pageConfig = testDefaultPageConfig.copy(optServiceName = None))
          ))

          verify(mockAuditConnector).sendExplicitAudit(
            auditType = "LimitedLiabilityPartnershipRegistration",
            detail = expectedAuditModel
          )
        }
      }
    }

    "the user is identifying a scottish limited partnership with all information" when {

      val journeyConfig = testDefaultGeneralPartnershipJourneyConfig.copy(partnershipType = ScottishLimitedPartnership)

      val expectedAuditModel: JsObject = expectedLimitedPartnershipAuditJson(partnershipType = "Scottish LTD Partnership")

      "identifiersMatch is false" should {
        "audit the correct information" in new Setup {
          when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
            limitedPartnershipFullJourneyData
          )))

          await(TestAuditService.auditPartnershipInformation(
            journeyId = testJourneyId,
            journeyConfig = journeyConfig
          ))

          verify(mockAuditConnector).sendExplicitAudit(
            auditType = "ScottishLTDPartnershipRegistration",
            detail = expectedAuditModel
          )
        }
      }

      "identifiersMatch is false and service name has not provided" should {
        "audit the correct information" in new Setup {
          when(mockPartnershipIdentificationService.retrievePartnershipFullJourneyData(testJourneyId)).thenReturn(Future.successful(Some(
            limitedPartnershipFullJourneyData
          )))

          when(mockAppConfig.defaultServiceName).thenReturn(testServiceName)

          await(TestAuditService.auditPartnershipInformation(
            journeyId = testJourneyId,
            journeyConfig = journeyConfig.copy(pageConfig = testDefaultPageConfig.copy(optServiceName = None))
          ))

          verify(mockAuditConnector).sendExplicitAudit(
            auditType = "ScottishLTDPartnershipRegistration",
            detail = expectedAuditModel
          )
        }
      }
    }
  }

  private def expectedNonLimitedPartnershipAuditJson(partnershipType: String,
                                                     verificationStatus: String,
                                                     registerStatus: String,
                                                     isMatch: String = "true"): JsObject = Json.obj(
    "SAUTR" -> testSautr,
    "SApostcode" -> testPostcode,
    "isMatch" -> isMatch,
    "businessType" -> partnershipType,
    "VerificationStatus" -> verificationStatus,
    "RegisterApiStatus" -> registerStatus,
    "callingService" -> testServiceName
  )

  private def expectedLimitedPartnershipAuditJson(partnershipType: String): JsObject = expectedNonLimitedPartnershipAuditJson(
    partnershipType = partnershipType,
    verificationStatus = "Not Enough Information to call BV",
    registerStatus = "not called",
    isMatch = "false"
  ) ++ Json.obj("companyNumber" -> testCompanyProfile.companyNumber)

}
