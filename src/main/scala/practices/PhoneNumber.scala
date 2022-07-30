package practices

import scala.util.parsing.combinator.*


final case class PhoneNumber private (prefix: Option[Char], digits: String) {
  private def copy() = ???

  override def toString(): String = s"${prefixStr}$digits"

  def prefixStr = prefix.map(_.toString()).getOrElse("")
}

final case class TrimmedString private (value: String) {
  private def copy() = ???
}

object TrimmedString {
  def mk(str: String) = TrimmedString(str.trim())
}

object PhoneNumber {

  final case class CreationError(msg: String)


  private def validateEmpty(str: String): Either[CreationError, TrimmedString] = {

    val trimmedStr = TrimmedString.mk(str)

    Either.cond(trimmedStr.value.nonEmpty, trimmedStr, CreationError(s"Expected +, 0-9, white-space. Got: ${str}"))
  }


  private def validatePrefixPlusSign(trimmedStr: TrimmedString): Either[CreationError, (Boolean, TrimmedString)] = {
    val str = trimmedStr.value
    val prefix = str.head

    if (prefix == '+') Right(true, trimmedStr)
    else if (prefix.isDigit) Right(false, trimmedStr)
    else Left(CreationError(s"First character should be '+' or 0-9 . Got: ${prefix}"))
  }


  private val containsAllDigitsAndAtLeast9Digits = (hasPlusSign: Boolean, trimmiedStr: TrimmedString) => {
    val (prefix -> str) = if (hasPlusSign) Some('+') -> trimmiedStr.value.drop(1) else None -> trimmiedStr.value

    val cond = str.forall(c => c.isDigit || c.isWhitespace) && str.count(_.isDigit) >= 9

    Either.cond(cond, prefix -> str, CreationError("Phone number should contains +, 0-9, white-space and at least 9 digit chars"))
  }


  def mk(str: String) = {

    validateEmpty(str)
      .flatMap(validatePrefixPlusSign)
      .flatMap(containsAllDigitsAndAtLeast9Digits.tupled)
      .map {
        case (maybePlusSign, str) => PhoneNumber(maybePlusSign, str)
      }
  }
}

