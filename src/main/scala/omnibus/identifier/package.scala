package omnibus

package object identifier {

  final case class InvalidIdentifier( rep: String )
      extends IllegalArgumentException( s"given identifier, ${rep}, is invalid" )
}
