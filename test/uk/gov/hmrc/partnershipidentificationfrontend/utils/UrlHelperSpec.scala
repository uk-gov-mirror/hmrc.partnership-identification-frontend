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

package uk.gov.hmrc.partnershipidentificationfrontend.utils

import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import uk.gov.hmrc.partnershipidentificationfrontend.config.AppConfig
import uk.gov.hmrc.partnershipidentificationfrontend.models.{JourneyConfig, PageConfig}

import uk.gov.hmrc.partnershipidentificationfrontend.helpers.TestConstants.{testDefaultGeneralPartnershipJourneyConfig, testDefaultPageConfig}

class UrlHelperSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  val mockAppConfig: AppConfig = mock[AppConfig]

  val allowedHosts: Set[String] = Set("localhost")

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAppConfig)
  }

  val target: UrlHelper = new UrlHelper(mockAppConfig)

  "UrlHelper" should {

    "allow journey configuration where all Url values are relative Urls" in {

      when(mockAppConfig.allowedHosts).thenReturn(Set())

      target.containsRelativeOrAcceptedUrlsOnly(testDefaultGeneralPartnershipJourneyConfig) mustBe true
    }

    "allow journey configurations where all Url values have hosts in the allowed hosts list" in {

      when(mockAppConfig.allowedHosts).thenReturn(allowedHosts)

      val testPageConfig: PageConfig = testDefaultPageConfig.copy(
        signOutUrl = "http://localhost:9000/signOut",
        accessibilityUrl = "http://localhost:9000/accessibility"
      )

      val testJourneyConfig: JourneyConfig = testDefaultGeneralPartnershipJourneyConfig.copy(
        continueUrl = "https://localhost:9000/continue",
        pageConfig = testPageConfig
      )

      target.containsRelativeOrAcceptedUrlsOnly(testJourneyConfig) mustBe true
    }

    "reject journey configuration when the continue url is absolute and the host is not in the allowed hosts list" in {

      when(mockAppConfig.allowedHosts).thenReturn(allowedHosts)

      val testJourneyConfig: JourneyConfig = testDefaultGeneralPartnershipJourneyConfig.copy(
        continueUrl = "http://somehost:9000/continue"
      )

      target.containsRelativeOrAcceptedUrlsOnly(testJourneyConfig) mustBe false
    }

    "reject journey configuration when the sign out url is absolute and the host is not in the allowed hosts list" in {

      when(mockAppConfig.allowedHosts).thenReturn(allowedHosts)

      val testPageConfig: PageConfig = testDefaultPageConfig.copy(
        signOutUrl = "https://somehost:9000/signOut"
      )

      val testJourneyConfig: JourneyConfig = testDefaultGeneralPartnershipJourneyConfig.copy(
        pageConfig = testPageConfig
      )

      target.containsRelativeOrAcceptedUrlsOnly(testJourneyConfig) mustBe false
    }

    "reject journey configuration when the accessibility url is absolute and the host is not in the allowed hosts list" in {

      when(mockAppConfig.allowedHosts).thenReturn(allowedHosts)

      val testPageConfig: PageConfig = testDefaultPageConfig.copy(
        accessibilityUrl = "https://somehost:9000/accessibility"
      )

      val testJourneyConfig: JourneyConfig = testDefaultGeneralPartnershipJourneyConfig.copy(
        pageConfig = testPageConfig
      )

      target.containsRelativeOrAcceptedUrlsOnly(testJourneyConfig) mustBe false
    }

    "reject journey configuration when the continue url is '/'" in {

      when(mockAppConfig.allowedHosts).thenReturn(allowedHosts)

      val testJourneyConfig: JourneyConfig = testDefaultGeneralPartnershipJourneyConfig.copy(
        continueUrl = "/"
      )

      target.containsRelativeOrAcceptedUrlsOnly(testJourneyConfig) mustBe false
    }

    "reject journey configuration when all urls are absolute and the allowed hosts list is empty" in {

        when(mockAppConfig.allowedHosts).thenReturn(Set())

        val testPageConfig: PageConfig = testDefaultPageConfig.copy(
          signOutUrl = "http://localhost:9000/signOut",
          accessibilityUrl = "http://localhost:9000/accessibility"
        )

        val testJourneyConfig: JourneyConfig = testDefaultGeneralPartnershipJourneyConfig.copy(
          continueUrl = "https://localhost:9000/continue",
          pageConfig = testPageConfig
        )

        target.containsRelativeOrAcceptedUrlsOnly(testJourneyConfig) mustBe false
      }

  }

}
