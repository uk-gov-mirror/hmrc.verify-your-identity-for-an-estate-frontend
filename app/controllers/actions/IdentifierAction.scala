/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.actions

import com.google.inject.Inject
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject()(
                                               authFunctions: AuthPartialFunctions,
                                               val parser: BodyParsers.Default
                                             )(implicit val executionContext: ExecutionContext) extends IdentifierAction with Logging {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    logger.info(s"[AuthenticatedIdentifierAction] identifying user")

    authFunctions.authorised().retrieve(Retrievals.internalId and Retrievals.credentials) {
      case Some(internalId) ~ Some(credentials) =>
          logger.info(s"[AuthenticatedIdentifierAction] user authenticated and retrieved internalId")
          block(IdentifierRequest(request, internalId, credentials))
      case _ =>
        throw new UnauthorizedException("Unable to retrieve internal Id")
    } recover authFunctions.recoverFromAuthorisation
  }
}