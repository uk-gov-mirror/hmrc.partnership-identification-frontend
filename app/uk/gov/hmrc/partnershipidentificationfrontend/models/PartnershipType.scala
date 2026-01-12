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

package uk.gov.hmrc.partnershipidentificationfrontend.models

import play.api.libs.json._

object PartnershipType {
  sealed trait PartnershipType

  case object GeneralPartnership extends PartnershipType

  case object ScottishPartnership extends PartnershipType

  case object ScottishLimitedPartnership extends PartnershipType

  case object LimitedPartnership extends PartnershipType

  case object LimitedLiabilityPartnership extends PartnershipType

  val GeneralPartnershipKey = "generalPartnership"
  val ScottishPartnershipKey = "scottishPartnership"
  val ScottishLimitedPartnershipKey = "scottishLimitedPartnership"
  val LimitedPartnershipKey = "limitedPartnership"
  val LimitedLiabilityPartnershipKey = "limitedLiabilityPartnership"

  implicit val format: Format[PartnershipType] = new Format[PartnershipType] {
    override def reads(json: JsValue): JsResult[PartnershipType] = json.validate[String].collect(JsonValidationError("Invalid partnership type")) {
      case GeneralPartnershipKey => GeneralPartnership
      case ScottishPartnershipKey => ScottishPartnership
      case ScottishLimitedPartnershipKey => ScottishLimitedPartnership
      case LimitedPartnershipKey => LimitedPartnership
      case LimitedLiabilityPartnershipKey => LimitedLiabilityPartnership
    }

    override def writes(partnershipType: PartnershipType): JsValue = partnershipType match {
      case GeneralPartnership => JsString(GeneralPartnershipKey)
      case ScottishPartnership => JsString(ScottishPartnershipKey)
      case ScottishLimitedPartnership => JsString(ScottishLimitedPartnershipKey)
      case LimitedPartnership => JsString(LimitedPartnershipKey)
      case LimitedLiabilityPartnership => JsString(LimitedLiabilityPartnershipKey)
    }
  }
}
