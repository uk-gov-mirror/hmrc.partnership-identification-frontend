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

package uk.gov.hmrc.partnershipidentificationfrontend.connectors

import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.partnershipidentificationfrontend.config.AppConfig
import uk.gov.hmrc.partnershipidentificationfrontend.httpparsers.ValidatePartnershipInformationHttpParser.ValidatePartnershipDetailsHttpReads

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ValidatePartnershipInformationConnector @Inject()(http: HttpClientV2,
                                                        appConfig: AppConfig) {

  def validate(sautr: String, postcode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    http.post(url = url"${appConfig.validatePartnershipInformationUrl}")(hc)
      .withBody(
        Json.obj(
          "sautr" -> sautr,
          "postcode" -> postcode
        )
      ).execute[Boolean]
}
