package practices

import scala.util.parsing.combinator.*

final case class EmailAddress private (localPart: EmailAddress.LocalPart, domain: EmailAddress.Domain) {
  private def copy() = ???

  override def toString(): String = s"${localPart.value}@${domain}"
}


// follow https://en.wikipedia.org/wiki/Email_address#Syntax
object EmailAddress {
  sealed trait CreationError
  object CreationError {
    final case class BeyondSizeLimit(limit: Int) extends CreationError
    final case class ParseError(msg: String) extends CreationError
  }

  final case class LocalPart private[EmailAddress] (value: String) extends AnyVal {
    private def copy() = ???
  }

  object LocalPart {
    private[EmailAddress] def mk(str: String) = {
      val sizeLimit = 64
      val err = CreationError.BeyondSizeLimit(sizeLimit)

      Either.cond(str.size <= sizeLimit, LocalPart(str.toLowerCase()), err)
    }
  }

  final case class Domain private[EmailAddress] (labels: Seq[String]) extends AnyVal {
    private def copy() = ???
    override def toString(): String = labels.mkString(".")
  }
  object Domain {
    private[EmailAddress] def mk(labels: Seq[String]) = {
      val sizeLimit = 63

      val err = CreationError.BeyondSizeLimit(sizeLimit)

      Either.cond(labels.forall(_.size <= sizeLimit), Domain(labels), err)

    }
  }

  def mk(str: String): Either[CreationError, EmailAddress] = {
    import UnquotedEmailAddressParser.*

    parseAll(emailAdressParser, str) match {

      case Success(localPartStr ~ _ ~ domainLabels, _) =>
        for {
          localPart <- LocalPart.mk(localPartStr)
          domain <- Domain.mk(domainLabels)
        } yield EmailAddress(localPart, domain)

      case Failure(msg,_) => Left(CreationError.ParseError(msg))

      case Error(msg,_) => Left(CreationError.ParseError(msg))
    }

  }


  object UnquotedEmailAddressParser extends RegexParsers {

    override def skipWhitespace: Boolean = false

    def printableChars: Parser[String] = "[!#$%&'*+\\-/=?^_`{|}~]+".r
    def words: Parser[String] = "[a-zA-Z0-9]+".r
    def dot: Parser[String] = "\\.".r

    def localPartParser: Parser[String] = {
      val validChars = (words | printableChars).+ ^^ { _.mkString }

      val dotChars = dot ~ validChars ^^ { case dot ~ chars => dot + chars.mkString }

      val parser = validChars ~ dotChars.* ^^ {
        case charses ~ dotCharses => charses.mkString + dotCharses.mkString
      }

      parser
    }

    def domainLabelsParser: Parser[List[String]] = {
      val hyphens: Parser[String] = "\\-+".r
      val hyphenWords = hyphens ~ words ^^ { case hyphen ~ words => hyphen + words }
      val label = words ~ hyphenWords.* ^^ {
        case words ~ hyphenWordses => words + hyphenWordses.mkString
      }

      val dotLabels = dot ~ label ^^ { case _ ~ labels => labels }

      val parser = label ~ dotLabels.+ ^^ {
        case label ~ dotLabelses => label +: dotLabelses
      }

      parser
    }


    def emailAdressParser = localPartParser ~ "@".r ~ domainLabelsParser
  }
}

