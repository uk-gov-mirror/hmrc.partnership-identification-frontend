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

package uk.gov.hmrc.partnershipidentificationfrontend.testonly.stubs.controllers

import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, InjectedController}

import javax.inject.{Inject, Singleton}

@Singleton
class CompaniesHouseStubController @Inject()(override val controllerComponents: ControllerComponents) extends InjectedController {

  private val companyNameKey = "company_name"
  private val companyNumberKey = "company_number"
  private val dateOfIncorporationKey = "date_of_creation"
  private val stubCompanyName = "Test Company Ltd"
  private val stubDateOfIncorporation = "2020-01-01"
  private val registeredOfficeAddressKey = "registered_office_address"

  private val stubRegisteredOfficeAddress = Json.obj(
    "address_line_1" -> "testLine1",
    "address_line_2" -> "test town",
    "care_of" -> "test name",
    "country" -> "United Kingdom",
    "locality" -> "test city",
    "po_box" -> "123",
    "postal_code" -> "AA11AA",
    "premises" -> "1",
    "region" -> "test region"
  )

  def getCompanyInformation(companyNumber: String): Action[AnyContent] = {

    val stubLtdPartnershipProfileWithNonMatchingPostcode = Json.obj(
      companyNameKey -> stubCompanyName,
      companyNumberKey -> companyNumber,
      dateOfIncorporationKey -> stubDateOfIncorporation,
      registeredOfficeAddressKey -> (stubRegisteredOfficeAddress + ("postal_code" -> JsString("BB11BB")))
    )

    val stubLtdPartnershipProfileWithNameLongerThan105Chars = {
      val companyNameLongerThan105Chars = "This company name is longer than 105 chars -" +
        " This company name is longer than 105 chars -" +
        " This company name is longer than 105 chars"

      require(
        requirement = companyNameLongerThan105Chars.length > 105,
        message = s"Company name $companyNameLongerThan105Chars should be longer than 105 chars but was ${companyNameLongerThan105Chars.length}"
      )

      Json.obj(
        companyNameKey -> companyNameLongerThan105Chars,
        companyNumberKey -> companyNumber,
        dateOfIncorporationKey -> stubDateOfIncorporation,
        registeredOfficeAddressKey -> stubRegisteredOfficeAddress
      )
    }

    Action {
      companyNumber match {
        case "00000001" => NotFound
        case "00000002" => Ok(stubLtdPartnershipProfileWithNonMatchingPostcode)
        case "00000003" => Ok(stubLtdPartnershipProfileWithNameLongerThan105Chars)
        case _ => Ok(stubLtdPartnershipProfile(companyNumber))
      }
    }
  }

  private def stubLtdPartnershipProfile(companyNumber: String): JsObject = {

    val officeAddress: JsObject = if(pillar2Data.contains(companyNumber)) {
      stubRegisteredOfficeAddress + ("postal_code" -> JsString(pillar2Data(companyNumber)))
    } else
      stubRegisteredOfficeAddress

    Json.obj(
      companyNameKey -> stubCompanyName,
      companyNumberKey -> companyNumber,
      dateOfIncorporationKey -> stubDateOfIncorporation,
      registeredOfficeAddressKey -> officeAddress
    )

  }

  lazy private val pillar2Data: Map[String, String] = Map(
    "31257555" -> "AV1 2CD",
    "31757555" -> "DH9 6TD",
    "12575555" -> "RH20 4EQ"
  )

}

