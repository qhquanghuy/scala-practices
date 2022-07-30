package practices

import java.time.LocalDate
import java.time.DayOfWeek

import org.scalacheck.Prop.*
import org.scalacheck.Properties
import org.scalacheck.*
import org.scalacheck.Test.Parameters


object CountSundaysInRangeTest extends Properties("countSundaysInRange") {

  val datePair = for {
    start <- Gen.choose(LocalDate.MIN, LocalDate.MAX)
    end <- Gen.choose(start, start.plusYears(10000)) // dont make test too slow because of iterating date over 100 tests
  } yield start -> end

  property("math calculation must be same the as count approach") = forAll(datePair) { (start: LocalDate, end: LocalDate) =>
    val dateRange = DateRangeInclusive.mk(start, `end`).toOption.get
    countSundaysInRange(start, end).toOption.get == dateRange.count(_.getDayOfWeek() == DayOfWeek.SUNDAY)
  }


  property("StartIsAfterEnd error if start after end") = forAll(datePair) { (start: LocalDate, end: LocalDate) =>
    countSundaysInRange(end, start) == Left(DateRangeInclusive.CreationError.StartIsAfterEnd)
  }

}