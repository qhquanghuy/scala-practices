package practices

import scala.util.Try

import org.scalacheck.Prop.*
import org.scalacheck.Properties
import org.scalacheck.*

object OrdinalIndicatorSuffixTest extends Properties("ordinalNumberString") {

  property("endsWith st | nd | rd | th") = forAll(Gen.posNum[Int]) { positiveInt =>
      ordinalNumberString(positiveInt).toOption.get.endsWith("st") ||
      ordinalNumberString(positiveInt).toOption.get.endsWith("rd") ||
      ordinalNumberString(positiveInt).toOption.get.endsWith("nd") ||
      ordinalNumberString(positiveInt).toOption.get.endsWith("th")
    }

  property("special case 11, 12, 13") = forAll(Gen.choose(11, 13)) { positiveInt =>
    ordinalNumberString(positiveInt).toOption.get == s"${positiveInt}th"
  }

  val ordinalNumberStringGen = {
    val maxLongDigitCount = Int.MaxValue.toString().size
    val suffix = (x: Char) => x match {
      case '1' => "st"
      case '2' => "nd"
      case '3' => "rd"
      case _ => "th"
    }
    for {
      size <- Gen.choose(1, maxLongDigitCount - 2)
      endingDigit <- Gen.numChar
      startingDigit <- Gen.numChar.filter(_ != '0')
      middles <- Gen.stringOfN(size, Gen.numChar)
      numStr = startingDigit + middles + endingDigit

      if (Try(numStr.toInt).isSuccess)

    } yield numStr + suffix(endingDigit)
  }

  property("generated ordinal number should be the same") = forAll(ordinalNumberStringGen) { ordinalStr =>
    val number = ordinalStr.dropRight(2).toInt
    ordinalNumberString(number).toOption.get == ordinalStr
  }


  property("error if negative integer") = forAll(Gen.negNum[Int]) { x =>
    ordinalNumberString(x).isLeft
  }

}