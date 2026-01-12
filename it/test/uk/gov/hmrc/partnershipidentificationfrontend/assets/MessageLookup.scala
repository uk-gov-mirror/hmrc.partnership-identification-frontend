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

object MessageLookup {

  object Base {
    val confirmAndContinue = "Confirm and continue"
    val change = "Change"
    val saveAndContinue = "Save and continue"
    val saveAndComeBack = "Save and come back later"
    val getHelp = "Is this page not working properly? (opens in new tab)"
    val back = "Back"
    val tryAgain = "Try again"
    val yes = "Yes"
    val no = "No"
    val continue = "Continue"

    object Error {
      val title = "There is a problem"
      val error = "Error: "
    }

  }

  object Header {
    val signOut = "Sign out"
  }

  object BetaBanner {
    val title = "This is a new service. Help us improve it and give your feedback by email."
  }

  object CaptureCompanyNumber {
    val title = "What is the company registration number? - Entity Validation Service - GOV.UK"
    val pageConfigTestTitle = "What is the company registration number? - Test Service - GOV.UK"
    val heading = "What is the company registration number?"
    val line_1 = "You can search Companies House for the company registration number (opens in new tab)."
    val link = "https://beta.companieshouse.gov.uk/"
    val hint = "This is 8 characters, like AB123456. It may also be called ‘company number’ or ‘Companies House number’"

    object Error {
      val noCompanyNumber = "Enter the company registration number"
      val invalidLengthCompanyNumber = "The company registration number must be 8 characters or fewer"
      val invalidCompanyNumber = "Enter the company registration number in the correct format"
    }
  }

  object ConfirmPartnershipName {
    val title = "Confirm the company name - Entity Validation Service - GOV.UK"
    val errorTitle = s"Error: $title"
    val pageConfigTestTitle = "Confirm the company name - Test Service - GOV.UK"
    val heading = "Is this your business?"
    val change_company_link = "Change company"
  }

  object CompanyNumberNotFound {
    val title = "The details you entered did not match our records - Entity Validation Service - GOV.UK"
    val pageConfigTestTitle = "The details you entered did not match our records - Test Service - GOV.UK"
    val heading = "The details you entered did not match our records"
    val line1 = "We could not match the details you entered with records held by HMRC."
    val line2 = "You can"
    val line2link = " search Companies House for the company registration number (opens in new tab)"
    val link = "https://beta.companieshouse.gov.uk/"
  }

  object CaptureSautr {
    val title = "Your partnership’s Self Assessment Unique Taxpayer Reference (UTR) - Entity Validation Service - GOV.UK"
    val pageConfigTestTitle = "Your partnership’s Self Assessment Unique Taxpayer Reference (UTR) - Test Service - GOV.UK"
    val heading = "Your partnership’s Self Assessment Unique Taxpayer Reference (UTR)"
    val line_1 = "You can find it in your Business Tax Account, the HMRC app or on tax returns and other documents from HMRC. It might be called ‘reference’, ‘UTR’ or ‘official use’."
    val link_1 = "Get more help to find your UTR (opens in new tab)"

    object Error {
      val saUtrNotEntered = "Enter the partnership’s Unique Taxpayer Reference"
      val invalidSautrEntered = "Enter the partnership’s Unique Taxpayer Reference in the correct format"
    }

  }

  object CapturePostCode {
    val title = "The postcode used to register the partnership for Self Assessment - Entity Validation Service - GOV.UK"
    val pageConfigTestTitle = "The postcode used to register the partnership for Self Assessment - Test Service - GOV.UK"
    val heading = "The postcode used to register the partnership for Self Assessment"
    val subTitle = "Enter the Self Assessment postcode"
    val details_summary = "Where to find the postcode on an example partnership for Self Assessment form"
    val alt_line = "An example partnership for Self Assessment form showing the postcode section on the left."
    val line_1 = "This information is on your partnership for Self Assessment form (SA400)"
    val line_2 = "Find the information in the ‘About the partnership’ section in box 2"
    val line_3 = "The postcode will be in the ‘Address of partnership’"
    val hint = "For example, AB1 2YZ"

    object Error {
      val noPostCodeEntered = "Enter the postcode used to register the partnership for Self Assessment"
      val invalidPostCodeEntered = "Enter the postcode in the correct format"
    }

  }

  object CannotConfirmBusiness {
    val title = "The details you provided do not match records held by HMRC - Entity Validation Service - GOV.UK"
    val errorTitle = s"Error: $title"
    val heading = "The details you provided do not match records held by HMRC"
    val line_1 = "If your details are correct, you can register and continue your journey. If not, go back and make changes."
    val radio = "Do you want to continue registering with the details you provided?"

    object Error {
      val no_selection = "Select yes if you want to continue registering with the details you provided"
    }

  }

  object CheckYourAnswers {
    val title = "Check your answers - Entity Validation Service - GOV.UK"
    val heading = "Check your answers"
    val sautr = "Unique Taxpayer Reference"
    val postCode = "Self Assessment postcode"
    val noSautr = "The business does not have a UTR"
    val companyNumber = "Company registration number"
    val internalServerErrorTitle = "Sorry, there is a problem with the service"
  }

}
