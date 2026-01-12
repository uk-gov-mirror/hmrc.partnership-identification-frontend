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
import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.partnershipidentificationfrontend.config.AppConfig
import uk.gov.hmrc.partnershipidentificationfrontend.forms.CaptureSautrForm
import uk.gov.hmrc.partnershipidentificationfrontend.models.PartnershipType._
import uk.gov.hmrc.partnershipidentificationfrontend.service.{JourneyService, PartnershipIdentificationService}
import uk.gov.hmrc.partnershipidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.partnershipidentificationfrontend.views.html.capture_sautr_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CaptureSautrController @Inject()(mcc: MessagesControllerComponents,
                                       sautr_view: capture_sautr_page,
                                       partnershipIdentificationService: PartnershipIdentificationService,
                                       journeyService: JourneyService,
                                       val authConnector: AuthConnector,
                                       messagesHelper: MessagesHelper
                                      )(implicit val config: AppConfig,
                                        executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {


  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.getJourneyConfig(journeyId, authInternalId).map {
            journeyConfig =>
              implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
              Ok(sautr_view(
                journeyId,
                journeyConfig.pageConfig,
                routes.CaptureSautrController.submit(journeyId),
                CaptureSautrForm.form,
                displayOrNotSkipSautrLink(partnershipType = journeyConfig.partnershipType)))
          }
        case _ =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          CaptureSautrForm.form.bindFromRequest().fold(
            formWithErrors => {
              journeyService.getJourneyConfig(journeyId, authInternalId).map {
                journeyConfig =>
                  implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
                  BadRequest(sautr_view(
                    journeyId,
                    journeyConfig.pageConfig,
                    routes.CaptureSautrController.submit(journeyId),
                    formWithErrors,
                    displayOrNotSkipSautrLink(partnershipType = journeyConfig.partnershipType))
                  )
              }
            },
            sautr =>
              partnershipIdentificationService.storeSautr(journeyId, sautr).map {
                _ => Redirect(routes.CapturePostCodeController.show(journeyId))
              }
          )
        case _ =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def noSautr(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.getJourneyConfig(journeyId, authInternalId).flatMap {
            _ =>
              partnershipIdentificationService.removeSaInformation(journeyId).map {
                _ => Redirect(routes.CheckYourAnswersController.show(journeyId))
              }
          }
        case _ =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  private def displayOrNotSkipSautrLink(partnershipType: PartnershipType): Boolean = partnershipType match {
    case GeneralPartnership | ScottishPartnership => true
    case ScottishLimitedPartnership | LimitedPartnership | LimitedLiabilityPartnership => false
  }

}
