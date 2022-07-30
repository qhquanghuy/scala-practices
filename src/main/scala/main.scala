
import java.time.*
import practices.*
@main def main = {
  println("hello, world")

  (0 to 30).map(ordinalNumberString).foreach(println)


  println(countSundaysInRange(LocalDate.parse("2021-05-01"), LocalDate.parse("2021-05-31"))) // 5


  println(maskPersonalInfo("local-part@domain-name.com")) // l*****t@domain-name.com

  println(maskPersonalInfo("+44 123 456 789")) // +**-***-**6-789


  println(maskPersonalInfo("+44 123 456 a789")) // Error

  println(maskPersonalInfo("adsfjadf..adfadsf@gm.cp")) // Error


}