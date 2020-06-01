package omnibus.identifier

import scala.reflect.ClassTag
import org.slf4j.LoggerFactory

object Snowflake {
  private val log = LoggerFactory.getLogger( "Snowflake" )

  private val workerIdBits = 5L
  private val maxWorkerId = -1L ^ ( -1L << workerIdBits)

  def workerIdFromHostForLabel( label: String ): Long = {
    val mac = {
      import java.net.NetworkInterface
      import scala.jdk.CollectionConverters._
      NetworkInterface.getNetworkInterfaces.asScala
        .filter { ni => Option( ni.getHardwareAddress ).isDefined }
        .foldLeft( 0 ) { ( acc, ni ) => 41 * ( 41 + ni.##) + acc }
    }

    val jvmName = java.lang.management.ManagementFactory.getRuntimeMXBean.getName

    log.info( s"calculate worker-id from: mac:[${mac}] jvm:[${jvmName}] label:[${label}]" )

    val hash =
      41 * (
        41 * (
          41 + label.##
        ) + jvmName.##
      ) + mac.##

    ( math.abs( hash ) % maxWorkerId).toLong
  }

  def workerIdFromHostFor[C: ClassTag]: Long = {
    val clazz = implicitly[ClassTag[C]].runtimeClass
    workerIdFromHostForLabel( clazz.getTypeName )
  }
}
