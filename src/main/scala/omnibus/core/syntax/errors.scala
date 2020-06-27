package omnibus.core.syntax

import cats.syntax.either._
import org.slf4j.{ Logger, LoggerFactory }
import omnibus.core.{ AllErrorsOr, AllIssuesOr, ErrorOr }

trait ErrorsSyntax {

  implicit def extractableIssues[A]( issues: AllIssuesOr[A] ): ExtractableIssues[A] = {
    new ExtractableIssues( issues )
  }

  implicit def extractableErrors[A]( errors: AllErrorsOr[A] ): ExtractableErrors[A] = {
    new ExtractableErrors( errors )
  }

  implicit def extractableError[A]( error: ErrorOr[A] ): ExtractableError[A] = {
    new ExtractableError( error )
  }

}

final class ExtractableIssues[A]( val underlying: AllIssuesOr[A] ) extends AnyVal {
  def log: Logger = LoggerFactory.getLogger( "ExtractableIssues" )

  def unsafeGet: A = {
    underlying valueOr { exs =>
      exs map { ex =>
        log.error( s"issue identified extracting validated value:[${underlying}]", ex )
      }
      throw exs.head
    }
  }

  def unsafeToErrorOr: ErrorOr[A] = {
    underlying.toEither
      .leftMap { exs =>
        exs map { ex =>
          log.error( s"error raised extracting value:[${underlying}]", ex )
        }
        exs.head
      }
  }
}

final class ExtractableErrors[A]( val underlying: AllErrorsOr[A] ) extends AnyVal {
  def log: Logger = LoggerFactory.getLogger( "ExtractableErrors" )

  def unsafeGet: A = {
    underlying valueOr { exs =>
      exs map { ex =>
        log.error( s"error raised extracting V value:[${underlying}]", ex )
      }
      throw exs.head
    }
  }

  def unsafeToErrorOr: ErrorOr[A] = {
    underlying leftMap { exs =>
      exs map { ex =>
        log.error( s"error raised extracting value:[${underlying}]", ex )
      }
      exs.head
    }
  }
}

final class ExtractableError[A]( val underlying: ErrorOr[A] ) extends AnyVal {
  def log: Logger = LoggerFactory.getLogger( "ExtractableError" )

  def unsafeGet: A = {
    underlying valueOr { ex =>
      log.error( s"error raised extracting TryV value:[${underlying}]", ex )
      throw ex
    }
  }
}
