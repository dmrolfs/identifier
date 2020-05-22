package omnibus.identifier

import scala.reflect.ClassTag
import org.slf4j.LoggerFactory

object Snowflake {

  private val workerIdBits = 5L
  private val maxWorkerId = -1L ^ ( -1L << workerIdBits)

  def workerIdFromHost[C: ClassTag](): Long = {
    val clazz = implicitly[ClassTag[C]].runtimeClass
    val log = LoggerFactory.getLogger( clazz )

    val mac = {
      import java.net.NetworkInterface
      import scala.jdk.CollectionConverters._
      NetworkInterface.getNetworkInterfaces.asScala
        .filter { ni => Option( ni.getHardwareAddress ).isDefined }
        .foldLeft( 0 ) { ( acc, ni ) => 41 * ( 41 + ni.##) + acc }
    }

    val jvmName = java.lang.management.ManagementFactory.getRuntimeMXBean.getName

    log.info(
      s"elements used to calculate worker-id: mac:[${mac}] jvm:[${jvmName}] class:[${clazz.getName}]"
    )

    val hash =
      41 * (
        41 * (
          41 + clazz.##
        ) + jvmName.##
      ) + mac.##

    ( math.abs( hash ) % maxWorkerId).toLong
  }
}
