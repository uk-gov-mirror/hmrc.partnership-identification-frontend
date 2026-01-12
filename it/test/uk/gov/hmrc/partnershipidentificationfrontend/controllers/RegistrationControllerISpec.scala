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

package uk.gov.hmrc.partnershipidentificationfrontend.controllers

import org.scalatest.concurrent.Eventually.eventually
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.partnershipidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.partnershipidentificationfrontend.models.BusinessVerificationStatus.format
import uk.gov.hmrc.partnershipidentificationfrontend.models.PartnershipType._
import uk.gov.hmrc.partnershipidentificationfrontend.models._
import uk.gov.hmrc.partnershipidentificationfrontend.stubs._
import uk.gov.hmrc.partnershipidentificationfrontend.utils.ComponentSpecHelper

class RegistrationControllerISpec extends ComponentSpecHelper with AuthStub with PartnershipIdentificationStub with RegisterStub with AuditStub {

  override lazy val extraConfig: Map[String, String] = Map(
    "auditing.enabled" -> "true",
    "auditing.consumer.baseUri.host" -> mockHost,
    "auditing.consumer.baseUri.port" -> mockPort
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  def auditJson(businessType: String, registerStatus: String, verificationStatus: String): JsObject =
    Json.obj(
      "SAUTR" -> testSautr,
      "SApostcode" -> testPostcode,
      "isMatch" -> "true",
      "businessType" -> businessType,
      "VerificationStatus" -> verificationStatus,
      "RegisterApiStatus" -> registerStatus,
      "callingService" -> testCallingServiceName
    )

  "GET /:journeyId/register" should {
    "redirect to continueUrl" when {
      "Business Verification is enabled" when {
        "a General Partnership registration is successful and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(GeneralPartnership, Some(testCallingServiceName), businessVerificationCheck = true, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = NOT_FOUND)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson[BusinessVerificationStatus](BusinessVerificationPass))
          stubRegisterGeneralPartnership(testSautr, testRegime)(status = OK, optBody = Some(Registered(testSafeId)))
          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJson)
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterGeneralPartnership(testSautr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
          eventually {
            verifyAuditDetail(auditJson("General Partnership", "success", "success"))
          }
        }

        "a General Partnership registration failed and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(GeneralPartnership, Some(testCallingServiceName), businessVerificationCheck = true, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = NOT_FOUND)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson[BusinessVerificationStatus](BusinessVerificationPass))
          stubRegisterGeneralPartnership(testSautr, testRegime)(status = OK, optBody = Some(testRegistrationFailedWith1Failure))
          stubStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJsonRegistrationFailed)
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterGeneralPartnership(testSautr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)
          eventually {
            verifyAuditDetail(auditJson("General Partnership", "fail", "success"))
          }
        }

        "a Scottish Partnership registration is successful and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(ScottishPartnership, Some(testCallingServiceName), businessVerificationCheck = true, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = NOT_FOUND)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson[BusinessVerificationStatus](BusinessVerificationPass))
          stubRegisterScottishPartnership(testSautr, testRegime)(status = OK, optBody = Some(Registered(testSafeId)))
          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJson)
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterScottishPartnership(testSautr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
          eventually {
            verifyAuditDetail(auditJson("Scottish Partnership", "success", "success"))
          }
        }

        "a Scottish Partnership registration failed and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(ScottishPartnership, Some(testCallingServiceName), businessVerificationCheck = true, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = NOT_FOUND)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson[BusinessVerificationStatus](BusinessVerificationPass))
          stubRegisterScottishPartnership(testSautr, testRegime)(status = OK, optBody = Some(testRegistrationFailedWith1Failure))
          stubStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJsonRegistrationFailed)
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterScottishPartnership(testSautr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)
          eventually {
            verifyAuditDetail(auditJson("Scottish Partnership", "fail", "success"))
          }
        }

        "a Limited Partnership registration is successful and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(LimitedPartnership, Some(testCallingServiceName), businessVerificationCheck = true, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson[BusinessVerificationStatus](BusinessVerificationPass))
          stubRegisterLimitedPartnership(testSautr, testCompanyNumber, testRegime)(status = OK, optBody = Some(Registered(testSafeId)))
          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJson)
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterLimitedPartnership(testSautr, testCompanyNumber, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
          eventually {
            verifyAuditDetail(auditJson("Limited Partnership", "success", "success"))
          }
        }

        "a Limited Partnership registration failed and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(LimitedPartnership, Some(testCallingServiceName), businessVerificationCheck = true, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson[BusinessVerificationStatus](BusinessVerificationPass))
          stubRegisterLimitedPartnership(testSautr, testCompanyNumber, testRegime)(status = OK, optBody = Some(testRegistrationFailedWith1Failure))
          stubStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJsonRegistrationFailed)
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterLimitedPartnership(testSautr, testCompanyNumber, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)
          eventually {
            verifyAuditDetail(auditJson("Limited Partnership", "fail", "success"))
          }
        }

        "a Scottish LTD Partnership registration is successful and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(ScottishLimitedPartnership, Some(testCallingServiceName), businessVerificationCheck = true, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson[BusinessVerificationStatus](BusinessVerificationPass))
          stubRegisterScottishLimitedPartnership(testSautr, testCompanyNumber, testRegime)(status = OK, optBody = Some(Registered(testSafeId)))
          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJson)
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterScottishLimitedPartnership(testSautr, testCompanyNumber, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
          eventually {
            verifyAuditDetail(auditJson("Scottish LTD Partnership", "success", "success"))
          }
        }

        "a Scottish LTD Partnership registration failed and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(ScottishLimitedPartnership, Some(testCallingServiceName), businessVerificationCheck = true, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson[BusinessVerificationStatus](BusinessVerificationPass))
          stubRegisterScottishLimitedPartnership(testSautr, testCompanyNumber, testRegime)(status = OK, optBody = Some(testRegistrationFailedWith1Failure))
          stubStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJsonRegistrationFailed)
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterScottishLimitedPartnership(testSautr, testCompanyNumber, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)
          eventually {
            verifyAuditDetail(auditJson("Scottish LTD Partnership", "fail", "success"))
          }
        }

        "a Limited Liability Partnership registration is successful and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(LimitedLiabilityPartnership, Some(testCallingServiceName), businessVerificationCheck = true, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson[BusinessVerificationStatus](BusinessVerificationPass))
          stubRegisterLimitedLiabilityPartnership(testSautr, testCompanyNumber, testRegime)(status = OK, optBody = Some(Registered(testSafeId)))
          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJson)
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterLimitedLiabilityPartnership(testSautr, testCompanyNumber, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
          eventually {
            verifyAuditDetail(auditJson("Limited Liability Partnership", "success", "success"))
          }
        }

        "a Limited Liability Partnership registration failed and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(LimitedLiabilityPartnership, Some(testCallingServiceName), businessVerificationCheck = true, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson[BusinessVerificationStatus](BusinessVerificationPass))
          stubRegisterLimitedLiabilityPartnership(testSautr, testCompanyNumber, testRegime)(status = OK, optBody = Some(testRegistrationFailedWith1Failure))
          stubStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJsonRegistrationFailed)
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterLimitedLiabilityPartnership(testSautr, testCompanyNumber, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)
          eventually {
            verifyAuditDetail(auditJson("Limited Liability Partnership", "fail", "success"))
          }
        }
      }
      "Business Verification is disabled" when {

        "a General Partnership registration is successful and registration status is successfully stored" in {
          await(insertJourneyConfig(
            testJourneyId,
            testInternalId,
            testJourneyConfig(GeneralPartnership, Some(testCallingServiceName), businessVerificationCheck = false, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = NOT_FOUND)
          stubRegisterGeneralPartnership(testSautr, testRegime)(status = OK, optBody = Some(Registered(testSafeId)))
          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJson - "businessVerification")
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterGeneralPartnership(testSautr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
          eventually {
            verifyAuditDetail(auditJson("General Partnership", "success", "not requested"))
          }
        }

        "a General Partnership registration failed and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(GeneralPartnership, Some(testCallingServiceName), businessVerificationCheck = false, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = NOT_FOUND)
          stubRegisterGeneralPartnership(testSautr, testRegime)(status = OK, optBody = Some(testRegistrationFailedWith1Failure))
          stubStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJsonRegistrationFailed - "businessVerification")
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterGeneralPartnership(testSautr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)
          eventually {
            verifyAuditDetail(auditJson("General Partnership", "fail", "not requested"))
          }
        }

        "a Scottish Partnership registration is successful and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(ScottishPartnership, Some(testCallingServiceName), businessVerificationCheck = false, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = NOT_FOUND)
          stubRegisterScottishPartnership(testSautr, testRegime)(status = OK, optBody= Some(Registered(testSafeId)))
          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJson - "businessVerification")
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterScottishPartnership(testSautr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
          eventually {
            verifyAuditDetail(auditJson("Scottish Partnership", "success", "not requested"))
          }
        }

        "a Scottish Partnership registration failed and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(ScottishPartnership, Some(testCallingServiceName), businessVerificationCheck = false, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = NOT_FOUND)
          stubRegisterScottishPartnership(testSautr, testRegime)(status = OK, optBody = Some(testRegistrationFailedWith1Failure))
          stubStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJsonRegistrationFailed - "businessVerification")
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterScottishPartnership(testSautr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)
          eventually {
            verifyAuditDetail(auditJson("Scottish Partnership", "fail", "not requested"))
          }
        }

        "a Limited Partnership registration is successful and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(LimitedPartnership, Some(testCallingServiceName), businessVerificationCheck = false, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRegisterLimitedPartnership(testSautr, testCompanyNumber, testRegime)(status = OK, optBody = Some(Registered(testSafeId)))
          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJson - "businessVerification")
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterLimitedPartnership(testSautr, testCompanyNumber, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
          eventually {
            verifyAuditDetail(auditJson("Limited Partnership", "success", "not requested"))
          }
        }

        "a Limited Partnership registration failed and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(LimitedPartnership, Some(testCallingServiceName), businessVerificationCheck = false, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRegisterLimitedPartnership(testSautr, testCompanyNumber, testRegime)(status = OK, optBody = Some(testRegistrationFailedWith1Failure))
          stubStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJsonRegistrationFailed - "businessVerification")
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterLimitedPartnership(testSautr, testCompanyNumber, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)
          eventually {
            verifyAuditDetail(auditJson("Limited Partnership", "fail", "not requested"))
          }
        }

        "a Limited Liability Partnership registration is successful and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(LimitedLiabilityPartnership, Some(testCallingServiceName), businessVerificationCheck = false, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRegisterLimitedLiabilityPartnership(testSautr, testCompanyNumber, testRegime)(status = OK, optBody = Some(Registered(testSafeId)))
          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJson - "businessVerification")
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterLimitedLiabilityPartnership(testSautr, testCompanyNumber, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
          eventually {
            verifyAuditDetail(auditJson("Limited Liability Partnership", "success", "not requested"))
          }
        }

        "a Limited Liability Partnership registration failed and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(LimitedLiabilityPartnership, Some(testCallingServiceName), businessVerificationCheck = false, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRegisterLimitedLiabilityPartnership(testSautr, testCompanyNumber, testRegime)(status = OK, optBody = Some(testRegistrationFailedWith1Failure))
          stubStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJsonRegistrationFailed - "businessVerification")
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterLimitedLiabilityPartnership(testSautr, testCompanyNumber, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)
          eventually {
            verifyAuditDetail(auditJson("Limited Liability Partnership", "fail", "not requested"))
          }
        }

        "a Scottish LTD Partnership registration is successful and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(ScottishLimitedPartnership, Some(testCallingServiceName), businessVerificationCheck = false, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRegisterScottishLimitedPartnership(testSautr, testCompanyNumber, testRegime)(status = OK, optBody = Some(Registered(testSafeId)))
          stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJson - "businessVerification")
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterScottishLimitedPartnership(testSautr, testCompanyNumber, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
          eventually {
            verifyAuditDetail(auditJson("Scottish LTD Partnership", "success", "not requested"))
          }
        }

        "a Scottish LTD Partnership registration failed and registration status is successfully stored" in {
          await(
            insertJourneyConfig(
              testJourneyId,
              testInternalId,
              testJourneyConfig(ScottishLimitedPartnership, Some(testCallingServiceName), businessVerificationCheck = false, testRegime)))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
          stubRetrieveCompanyProfile(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRegisterScottishLimitedPartnership(testSautr, testCompanyNumber, testRegime)(status = OK, optBody = Some(testRegistrationFailedWith1Failure))
          stubStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)(status = OK)
          stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJsonRegistrationFailed - "businessVerification")
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisterScottishLimitedPartnership(testSautr, testCompanyNumber, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, testRegistrationFailedWith1Failure)
          eventually {
            verifyAuditDetail(auditJson("Scottish LTD Partnership", "fail", "not requested"))
          }
        }
      }
    }

    "redirect to SignInPage" when {
      "the user is unauthorised" in {
        stubAuthFailure()

        val result = get(s"$baseUrl/$testJourneyId/register")

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(s"/bas-gateway/sign-in?continue_url=%2Fidentify-your-partnership%2F$testJourneyId%2Fregister&origin=partnership-identification-frontend")
      }
    }

    "throw an exception" when {
      "business verification is in an invalid state" in {
        await(insertJourneyConfig(testJourneyId, testInternalId, testGeneralPartnershipJourneyConfig(businessVerificationCheck = true)))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.obj("verificationStatus" -> "Invalid"))

        val result = get(s"$baseUrl/$testJourneyId/register")

        result.status mustBe INTERNAL_SERVER_ERROR
      }

      "business verification returns fail" in {
        await(insertJourneyConfig(testJourneyId, testInternalId, testJourneyConfig(GeneralPartnership, Some(testCallingServiceName), businessVerificationCheck = true, testRegime)))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
        stubRetrieveCompanyProfile(testJourneyId)(status = NOT_FOUND)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson[BusinessVerificationStatus](BusinessVerificationFail))
        stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(status = OK)
        stubRetrievePartnershipDetails(testJourneyId)(OK, testPartnershipFullJourneyDataJsonBVFailed)
        stubAudit()

        val result = get(s"$baseUrl/$testJourneyId/register")

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
        verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
        eventually {
          verifyAuditDetail(auditJson("General Partnership", "not called", "fail"))
        }
      }

      "sautr is missing" in {
        await(insertJourneyConfig(testJourneyId, testInternalId, testGeneralPartnershipJourneyConfig(businessVerificationCheck = true)))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveSautr(testJourneyId)(status = NOT_FOUND)

        val result = get(s"$baseUrl/$testJourneyId/register")

        result.status mustBe INTERNAL_SERVER_ERROR
      }

      "business verification status is missing" in {
        await(insertJourneyConfig(testJourneyId, testInternalId, testGeneralPartnershipJourneyConfig(businessVerificationCheck = true)))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = NOT_FOUND)

        val result = get(s"$baseUrl/$testJourneyId/register")

        result.status mustBe INTERNAL_SERVER_ERROR
      }

      "the backend service returns a bad request status" in {
        await(insertJourneyConfig(testJourneyId, testInternalId, testGeneralPartnershipJourneyConfig(businessVerificationCheck = true)))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveSautr(testJourneyId)(status = BAD_REQUEST)

        val result = get(s"$baseUrl/$testJourneyId/register")

        result.status mustBe INTERNAL_SERVER_ERROR
      }

      "the backend service returns an internal server error" in {
        await(insertJourneyConfig(testJourneyId, testInternalId, testGeneralPartnershipJourneyConfig(businessVerificationCheck = true)))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveSautr(testJourneyId)(status = INTERNAL_SERVER_ERROR)

        val result = get(s"$baseUrl/$testJourneyId/register")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
