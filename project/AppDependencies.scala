import play.core.PlayVersion
import sbt.*

object AppDependencies {

  private val bootstrapPlayVersion = "10.5.0"
  private val hmrcMongoVersion = "2.11.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30"  % bootstrapPlayVersion,
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30" % "12.25.0",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"         % hmrcMongoVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.19.2"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"  % bootstrapPlayVersion,
    "org.scalatest"          %% "scalatest"               % "3.2.19",
    "org.jsoup"               % "jsoup"                   % "1.21.2",
    "org.playframework"      %% "play-test"               % PlayVersion.current,
    "com.vladsch.flexmark"    % "flexmark-all"            % "0.64.8",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "7.0.2",
    "com.github.tomakehurst"  % "wiremock"                % "3.8.0",
    "org.scalatestplus"      %% "mockito-5-10"            % "3.2.18.0",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30" % hmrcMongoVersion
  ).map(_ % "test")
}
