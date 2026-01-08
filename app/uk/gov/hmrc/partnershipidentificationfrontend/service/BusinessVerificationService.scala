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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.partnershipidentificationfrontend.connectors.{CreateBusinessVerificationJourneyConnector, RetrieveBusinessVerificationStatusConnector}
import uk.gov.hmrc.partnershipidentificationfrontend.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessVerificationService @Inject()(createBusinessVerificationJourneyConnector: CreateBusinessVerificationJourneyConnector,
                                            retrieveBusinessVerificationStatusConnector: RetrieveBusinessVerificationStatusConnector,
                                            partnershipIdentificationService: PartnershipIdentificationService
                                           )(implicit val executionContext: ExecutionContext) {

  def createBusinessVerificationJourney(journeyId: String, sautr: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[Option[String]] =
    createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(journeyId, sautr, journeyConfig).flatMap {
      case Right(JourneyCreated(redirectUrl)) => Future.successful(Option(redirectUrl))
      case Left(failureReason) =>
        val bvStatus = failureReason match {
          case NotEnoughEvidence => BusinessVerificationNotEnoughInformationToChallenge
          case UserLockedOut => BusinessVerificationFail
        }
        partnershipIdentificationService.storeBusinessVerificationStatus(journeyId, bvStatus).map {
          _ => None
        }
    }

  def retrieveBusinessVerificationStatus(businessVerificationJourneyId: String)(implicit hc: HeaderCarrier): Future[BusinessVerificationStatus] =
    retrieveBusinessVerificationStatusConnector.retrieveBusinessVerificationStatus(businessVerificationJourneyId)

}
