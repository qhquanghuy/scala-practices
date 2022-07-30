package practices


private def maskEmailAddress(email: EmailAddress) = {
  val localPartStr = email.localPart.value
  val localPartSize = localPartStr.size
  val masked = if (localPartStr.size < 2) {
    List.fill(localPartSize)("*").mkString
  } else localPartStr.head + "*****" + localPartStr.last

  s"${masked}@${email.domain.toString}"
}



private def maskPhoneNumber(phoneNumber: PhoneNumber) = {
  val digitSize = phoneNumber.digits.size


  val (masked, _) = phoneNumber.digits.foldRight("" -> 0) {
    case (currentDigit, (str, digitCount)) =>
      if (currentDigit.isWhitespace) ('-' +: str) -> digitCount
      else if (currentDigit.isDigit && digitCount < 4) (currentDigit +: str) -> (digitCount + 1)
      else ('*' +: str) -> (digitCount + 1)
  }

  phoneNumber.prefixStr + masked
}

def maskPersonalInfo(str: String): Either[String, String] = {
  val maskedEmailAddress = EmailAddress.mk(str).map(maskEmailAddress)
  val maskedPhoneNo = PhoneNumber.mk(str).map(maskPhoneNumber)

  maskedEmailAddress.orElse(maskedPhoneNo)
    .left.map(_ => s"$str is not a valid email address or phone number")
}