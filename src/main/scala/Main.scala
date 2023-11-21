
import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.neptunedata.NeptunedataClient
import software.amazon.awssdk.services.neptunedata.model.{GetLoaderJobStatusRequest, StartLoaderJobRequest}

import java.net.URI
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object Main {

  implicit val system: ActorSystem = ActorSystem("test")

  def main(args: Array[String]): Unit = {
    println("Starting loader job")

    startAndWaitLoaderJob()

    println("Loader job complete")
  }

  private val neptuneCluster: URI =
    URI.create(scala.util.Properties.envOrElse("AWS_NEPTUNE_INSTANCE",""))
  private val region: String = scala.util.Properties.envOrElse("AWS_DEFAULT_REGION","")
  private val s3BulkImportBucket: String = scala.util.Properties.envOrElse("AWS_S3_BULK_IMPORT_BUCKET","")
  private val neptuneReadS3IamRoleArn: String = scala.util.Properties.envOrElse("AWS_NEPTUNE_ROLE_ARN", "")

  private val client = NeptunedataClient
    .builder()
    .region(Region.CA_CENTRAL_1)
    .endpointOverride(neptuneCluster)
    .build()

  def startAndWaitLoaderJob(): Done = {
    val req = StartLoaderJobRequest.builder()
      .format("csv")
      .s3BucketRegion(region)
      .source(s3BulkImportBucket)
      .iamRoleArn(neptuneReadS3IamRoleArn)
      .failOnError(false)
      .updateSingleCardinalityProperties(true)
      .build()

    val res = client.startLoaderJob(req)

    val loadId = res.payload().get("loadId")
    Await.result(waitForLoader(loadId), 30.minutes)
  }

  private def waitForLoader(loadId: String) =
    Source(1 to 240)
      .throttle(1, 5.second)
      .map(_ => getLoaderStatus(loadId))
      .takeWhile(identity)
      .run()

  private def getLoaderStatus(loadId: String) = {
    val req = GetLoaderJobStatusRequest.builder().loadId(loadId).build()
    val status = client.getLoaderJobStatus(req)
    status.payload().asMap().get("overallStatus").asMap().get("status").toString == "LOAD_COMPLETED"
  }
}
