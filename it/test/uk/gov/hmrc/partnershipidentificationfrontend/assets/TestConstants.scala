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

package uk.gov.hmrc.partnershipidentificationfrontend.assets

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.partnershipidentificationfrontend.models.PartnershipType._
import uk.gov.hmrc.partnershipidentificationfrontend.models.{PartnershipType => _, _}

import java.time.LocalDate
import java.util.UUID

object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testSautr: String = "1234567890"
  val testPostcode: String = "AA11AA"
  val testRegime = "VATC"
  val testRegisteredOfficePostcode: String = "BB11BB"
  val testContinueUrl: String = "/continue"
  val testCredentialId: String = UUID.randomUUID().toString
  val GGProviderId: String = UUID.randomUUID().toString
  val testGroupId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString
  val testBusinessVerificationJourneyId: String = UUID.randomUUID().toString
  val testSafeId: String = UUID.randomUUID().toString
  val testCompanyNumber: String = "12345678"
  val testCompanyName: String = "Test Company Ltd"
  val testCtutr: String = "1234567890"
  val testDOIYear: Int = 2000
  val testDateOfIncorporation: String = LocalDate.of(testDOIYear, 1, 1).toString
  val testAddress: JsObject = Json.obj(
    "address_line_1" -> "testLine1",
    "address_line_2" -> "test town",
    "care_of" -> "test name",
    "country" -> "United Kingdom",
    "locality" -> "test city",
    "po_box" -> "123",
    "postal_code" -> testRegisteredOfficePostcode,
    "premises" -> "1",
    "region" -> "test region"
  )
  val testCompanyProfile: CompanyProfile = CompanyProfile(testCompanyName, testCompanyNumber, testDateOfIncorporation, testAddress)
  val testDeskProServiceId: String = "vrs"
  val testSignOutUrl: String = "/Signout"
  val testAccessibilityUrl: String = "/accessibility"
  val testTechnicalHelpUrl: String = "http://localhost:9250/contact/report-technical-problem?service=grs"
  val testDefaultServiceName: String = "Entity Validation Service"
  val testCallingServiceName: String = "Test Service"

  val testDefaultPageConfig: PageConfig =  PageConfig(
    optServiceName = None,
    deskProServiceId = testDeskProServiceId,
    signOutUrl = testSignOutUrl,
    accessibilityUrl = testAccessibilityUrl,
    optLabels = None
  )

  val testDefaultJourneyConfig: JourneyConfig = JourneyConfig(
    testContinueUrl,
    businessVerificationCheck = true,
    testDefaultPageConfig,
    partnershipType = GeneralPartnership,
    regime = testRegime
  )


  val testWelshServiceName: String = "This is a welsh service name"
  val testEnglishServiceName: String = "This is a welsh service name"

  val testDefaultWelshJourneyConfig: JourneyConfig = testDefaultJourneyConfig.copy(
    pageConfig = testDefaultPageConfig.copy(optLabels = Some(JourneyLabels(optWelshServiceName = Some(testWelshServiceName), None))
    )
  )

  val testDefaultWelshServiceName: String = "Gwasanaeth Dilysu Endid"

  def testJourneyConfig(partnershipType: PartnershipType,
                        serviceName: Option[String] = None,
                        businessVerificationCheck: Boolean,
                        regime: String): JourneyConfig =
    JourneyConfig(
      testContinueUrl,
      businessVerificationCheck,
      PageConfig(serviceName, testDeskProServiceId, testSignOutUrl, testAccessibilityUrl),
      partnershipType,
      regime)

  def testGeneralPartnershipJourneyConfig(businessVerificationCheck: Boolean): JourneyConfig =
    testJourneyConfig(GeneralPartnership, businessVerificationCheck = businessVerificationCheck, regime = testRegime)

  def testGeneralPartnershipJourneyConfigWithCallingService(businessVerificationCheck: Boolean): JourneyConfig =
    testJourneyConfig(
      GeneralPartnership,
      serviceName = Some(testCallingServiceName),
      businessVerificationCheck = businessVerificationCheck,
      regime = testRegime)

  def testScottishPartnershipJourneyConfig(businessVerificationCheck: Boolean): JourneyConfig =
    testJourneyConfig(ScottishPartnership, businessVerificationCheck = businessVerificationCheck, regime = testRegime)

  def testScottishLimitedPartnershipJourneyConfig(businessVerificationCheck: Boolean): JourneyConfig =
    testJourneyConfig(ScottishLimitedPartnership, businessVerificationCheck = businessVerificationCheck, regime = testRegime)

  def testLimitedPartnershipJourneyConfig(businessVerificationCheck: Boolean): JourneyConfig =
    testJourneyConfig(LimitedPartnership, businessVerificationCheck = businessVerificationCheck, regime = testRegime)

  def testLimitedLiabilityPartnershipJourneyConfig(businessVerificationCheck: Boolean): JourneyConfig =
    testJourneyConfig(LimitedLiabilityPartnership, businessVerificationCheck = businessVerificationCheck, regime = testRegime)

  val testPartnershipInformationJson: JsObject = {
    Json.obj(
      "sautr" -> testSautr,
      "postcode" -> testPostcode
    )
  }

  val testPartnershipInformationWithCompanyProfile: JsObject =
    Json.obj(
      "sautr" -> testSautr,
      "postcode" -> testPostcode,
      "companyProfile" -> Json.obj(
        "companyName" -> "Test Company Ltd",
        "companyNumber" -> testCompanyNumber,
        "dateOfIncorporation" -> testDateOfIncorporation,
        "unsanitisedCHROAddress" -> Json.obj(
          "address_line_1" -> "testLine1",
          "address_line_2" -> "test town",
          "care_of" -> "test name",
          "country" -> "United Kingdom",
          "locality" -> "test city",
          "po_box" -> "123",
          "postal_code" -> testRegisteredOfficePostcode,
          "premises" -> "1",
          "region" -> "test region"
        )
      )
    )


  val testPartnershipFullJourneyDataJson: JsObject = {
    Json.obj(
      "sautr" -> testSautr,
      "postcode" -> testPostcode,
      "identifiersMatch" -> "IdentifiersMatched",
      "businessVerification" -> Json.obj(
        "verificationStatus" -> "PASS"
      ),
      "registration" -> Json.obj(
        "registrationStatus" -> "REGISTERED",
        "registeredBusinessPartnerId" -> testSafeId
      )
    )
  }

  val testPartnershipFullJourneyDataJsonRegistrationFailed: JsObject = {
    Json.obj(
      "sautr" -> testSautr,
      "postcode" -> testPostcode,
      "identifiersMatch" -> "IdentifiersMatched",
      "businessVerification" -> Json.obj(
        "verificationStatus" -> "PASS"
      ),
      "registration" -> Json.obj(
        "registrationStatus" -> "REGISTRATION_FAILED",
        "failures" -> Json.arr(
          Json.obj(
            "code" -> "INVALID_REGIME",
            "reason" -> "Request has not passed validation. Invalid regime"
          ),
          Json.obj(
            "code" -> "INVALID_PAYLOAD",
            "reason" -> "Request has not passed validation. Invalid payload."
          )
        )
      )
    )
  }

  val testPartnershipFullJourneyDataJsonWithCompanyProfile: JsObject = {
    Json.obj(
      "sautr" -> testSautr,
      "postcode" -> testPostcode,
      "identifiersMatch" -> "IdentifiersMismatch",
      "businessVerification" -> Json.obj(
        "verificationStatus" -> "NOT_ENOUGH_INFORMATION_TO_CALL_BV"
      ),
      "registration" -> Json.obj(
        "registrationStatus" -> "REGISTRATION_NOT_CALLED"
      ),
      "companyProfile" -> Json.obj(
        "companyName" -> testCompanyName,
        "companyNumber" -> testCompanyNumber,
        "dateOfIncorporation" -> testDateOfIncorporation,
        "unsanitisedCHROAddress" -> Json.obj(
          "address_line_1" -> "testLine1",
          "address_line_2" -> "test town",
          "care_of" -> "test name",
          "country" -> "United Kingdom",
          "locality" -> "test city",
          "po_box" -> "123",
          "postal_code" -> testRegisteredOfficePostcode,
          "premises" -> "1",
          "region" -> "test region"
        )
      )
    )
  }


  val testPartnershipFullJourneyData: PartnershipFullJourneyData =
    PartnershipFullJourneyData(
      Some(testPostcode),
      Some(testSautr),
      None,
      identifiersMatch = IdentifiersMatched,
      Some(BusinessVerificationPass),
      Registered(testSafeId))

  def testLimitedPartnershipAuditJson(businessType: String): JsObject = Json.obj(
    "SAUTR" -> testSautr,
    "SApostcode" -> testPostcode,
    "isMatch" -> "false",
    "businessType" -> businessType,
    "VerificationStatus" -> "Not Enough Information to call BV",
    "RegisterApiStatus" -> "not called",
    "callingService" -> testDefaultServiceName,
    "companyNumber" -> testCompanyNumber
  )

  val testRegistrationFailedWithMultipleFailures: RegistrationFailed = RegistrationFailed(
    Array(
      Failure("INVALID_REGIME", "Request has not passed validation. Invalid regime"),
      Failure("INVALID_PAYLOAD", "Request has not passed validation. Invalid payload.")
    )
  )

  val testRegistrationFailedWith1Failure: RegistrationFailed = RegistrationFailed(
    Array(
      Failure("PARTY_TYPE_MISMATCH", "The remote endpoint has indicated there is Party Type mismatch")
    ))

  val testPartnershipFullJourneyDataJsonBVFailed: JsObject = {
    Json.obj(
      "sautr" -> testSautr,
      "postcode" -> testPostcode,
      "identifiersMatch" -> "IdentifiersMatched",
      "businessVerification" -> Json.obj(
        "verificationStatus" -> "FAIL"
      ),
      "registration" -> Json.obj(
        "registrationStatus" -> "REGISTRATION_NOT_CALLED"
      )
    )
  }

  val signInRedirectUrl: (String, String) => String = (journeyId, currentPageUrl) =>
    "/bas-gateway/sign-in" +
      s"?continue_url=%2Fidentify-your-partnership%2F$journeyId%2F$currentPageUrl" +
      "&origin=partnership-identification-frontend"

}
