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

import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.partnershipidentificationfrontend.config.AppConfig
import uk.gov.hmrc.partnershipidentificationfrontend.forms.ConfirmPartnershipNameForm.confirmPartnershipNameForm
import uk.gov.hmrc.partnershipidentificationfrontend.service.{JourneyService, PartnershipIdentificationService}
import uk.gov.hmrc.partnershipidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.partnershipidentificationfrontend.views.html.confirm_partnership_name_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmPartnershipNameController @Inject()(mcc: MessagesControllerComponents,
                                                 val authConnector: AuthConnector,
                                                 journeyService: JourneyService,
                                                 partnershipIdentificationService: PartnershipIdentificationService,
                                                 view: confirm_partnership_name_page,
                                                 messagesHelper: MessagesHelper
                                                )(implicit val executionContext: ExecutionContext,
                                                  appConfig: AppConfig) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.getJourneyConfig(journeyId, authInternalId).flatMap {
            journeyConfig =>
              partnershipIdentificationService.retrieveCompanyProfile(journeyId).map {
                case Some(companiesHouseInformation) =>
                  implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
                  Ok(view(journeyConfig.pageConfig,
                    routes.ConfirmPartnershipNameController.submit(journeyId),
                    companiesHouseInformation.companyName,
                    journeyId,
                    confirmPartnershipNameForm
                  ))
                case None =>
                  throw new InternalServerException("No company profile stored")
              }
          }
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          confirmPartnershipNameForm.bindFromRequest().fold(
            formWithErrors =>
              journeyService.getJourneyConfig(journeyId, authInternalId).flatMap {
                journeyConfig =>
                  partnershipIdentificationService.retrieveCompanyProfile(journeyId).map {
                    case Some(companiesHouseInformation) =>
                      implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
                      BadRequest(view(journeyConfig.pageConfig,
                        routes.ConfirmPartnershipNameController.submit(journeyId),
                        companiesHouseInformation.companyName,
                        journeyId,
                        formWithErrors
                      ))
                    case None =>
                      throw new InternalServerException("No company profile stored")
                  }
              },
            formRadio =>
              if (formRadio) Future.successful(Redirect(routes.CaptureSautrController.show(journeyId)))
              else Future.successful(Redirect(routes.CaptureCompanyNumberController.show(journeyId)))
          )
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

}
