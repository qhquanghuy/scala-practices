package practices

import java.time.LocalDate
import java.time.DayOfWeek

import org.scalacheck.Prop.*
import org.scalacheck.Properties
import org.scalacheck.*
import collection.convert.ImplicitConversions.*
import scala.util.Random


object MaskPersonalInfoTests extends Properties("maskPersonalInfo") {

  val printableCharGen = Gen.oneOf(Seq('!', '#', '$', '%', '&', '*' , '+' , '-' , '/', '=', '?', '^', '_', '`' , '{', '|', '}', '~'))

  def replaceCharsGen(maxLen: Int, validChars: Gen[Char], randomReplaceChar: Char = '.') = {
    def replaceCharRandomly(str: String) = {
      val strLen = str.size

      strLen match {
        case 1 => Gen.const(str)
        case 2 => Gen.const(str.updated(0, randomReplaceChar))
        case 3 => Gen.const(str.updated(1, randomReplaceChar))
        case _ => Gen.choose(1, strLen - 2).map(str.updated(_, randomReplaceChar))
      }

    }

    for {
      strLen <- Gen.choose(1, maxLen)
      str <- Gen.stringOfN(strLen, validChars)

      firstPartLen <- Gen.choose(1, maxLen)

      (firstPart, secondPart) = str.splitAt(firstPartLen)

      replacedStr <- if (secondPart.isEmpty()) Gen.const(firstPart) else {
        val maxReplacement = (secondPart.size - 1) / 2

        Gen.choose(0, maxReplacement)
          .map(numReplacement => if (numReplacement == 0) Seq(secondPart) else secondPart.grouped(numReplacement).toList)
          .map(_.map(replaceCharRandomly))
          .flatMap(Gen.sequence)
          .map { _.mkString }
          .map(firstPart + _)
      }


    } yield replacedStr
  }


  val localPartGen = {
    val validChars = Gen.oneOf(Gen.alphaChar, Gen.numChar, printableCharGen)

    replaceCharsGen(64, validChars)
  }

  val domainGen = {
    val gen = replaceCharsGen(63, Gen.oneOf(Gen.alphaChar, Gen.numChar), '-')

    Gen.choose(2, 100).flatMap(n => Gen.listOfN(n, gen)).map(_.mkString("."))
  }

  property("localPartGen should gen string less than 64 chars") = forAll(localPartGen) { localPart =>
    localPart.size <= 64
  }

  property("localPartGen should gen string without ..") = forAll(localPartGen) { localPart =>
    localPart.contains("..") == false
  }

  property("localPartGen should gen string not starts and ends with .") = forAll(localPartGen) { localPart =>
    localPart.head != '.' && localPart.last != '.'
  }

  property("domainGen label should gen string less than 63") = forAll(domainGen) { domain =>
    domain.split('.').forall(_.size <= 63)
  }

  property("domainGen label should gen string splited by dot") = forAll(domainGen) { domain =>
    domain.split('.').size > 1
  }


  property("masked email domain is the same as original") = forAll(localPartGen, domainGen) { (localPart, domain) =>
    val email = s"$localPart@$domain"

    val maskedEmail = maskPersonalInfo(email).toOption.get

    val maskedEmailDomain = maskedEmail.split('@').last

    domain == maskedEmailDomain
  }

  property("should masked with * or *****, un-masked char in lower cased") = forAll(localPartGen, domainGen) { (localPart, domain) =>
    val email = s"$localPart@$domain"

    val maskedEmail = maskPersonalInfo(email).toOption.get

    val maskedLocalPart = maskedEmail.split('@').head

    maskedLocalPart == "*" || maskedLocalPart == localPart.head.toString().toLowerCase + "*****" + localPart.last.toString().toLowerCase()
  }


  property("should return error if not email or phonenumber") = forAll { (str: String) =>
    maskPersonalInfo(str.filterNot(c => c == '@' || c.isDigit).mkString).isLeft
  }

  property("should return error if phonenumber less than 9 digit chars") = forAll(
    Gen.choose(0, 8).flatMap(Gen.stringOfN(_, Gen.numChar))
  ) { (str: String) =>
    maskPersonalInfo(str).isLeft
  }


  val phoneNumberGen = {

    val gen = Gen.choose(9, 100).flatMap(Gen.stringOfN(_, Gen.numChar))

    Gen.oneOf(
      gen,
      gen.map(str => s"+$str")
    )
    .map { str =>

      str.flatMap(c => if (Random.nextBoolean()) Seq(c) else Seq(c, ' ')).mkString
    }
  }


  property("masked phonenumber include only +,-,* and reveal 4 digit") = forAll(phoneNumberGen) { (str: String) =>
    val maskedPhoneNo = maskPersonalInfo(str).toOption.get



    val (partNotHavingDigits, partHavingDigit) = maskedPhoneNo.splitAt(maskedPhoneNo.indexWhere(_.isDigit))

    val firstMasked = partNotHavingDigits.filter(_ != '+').mkString

    """[*-]+""".r.matches(firstMasked) && partHavingDigit.count(_.isDigit) == 4
  }

}
