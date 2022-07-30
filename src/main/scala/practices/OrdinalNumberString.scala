package practices


final case class PositiveInt private (value: Int) extends AnyVal {
  private def copy() = ???
}

object PositiveInt {

  /**
   * []
   *
   * @return None when x <= 0
   */
  def mk(x: Int): Option[PositiveInt] = {
    Option.when(x > 0)(PositiveInt(x))
  }
}


def ordinalNumberString(positiveInt: PositiveInt) = {
  val value = positiveInt.value
  val lastDigit = value % 10

  val suffix = lastDigit match {
    case 1 if value != 11 => "st"
    case 2 if value != 12 => "nd"
    case 3 if value != 13 => "rd"
    case _ => "th"
  }

  s"${value}${suffix}"
}


def ordinalNumberString(x: Int): Either[String, String] = {
  PositiveInt.mk(x).map(ordinalNumberString)
    .map(Right.apply)
    .getOrElse(Left(s"Bad Input: ${x}. Expected postive integer"))
}