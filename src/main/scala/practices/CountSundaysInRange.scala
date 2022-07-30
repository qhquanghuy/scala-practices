package practices

import java.time.LocalDate
import java.time.DayOfWeek
import java.time.Period
import java.time.temporal.ChronoUnit
import practices.DateRangeInclusive.CreationError

final case class DateRangeInclusive private (start: LocalDate, end: LocalDate) extends Iterable[LocalDate] {
  private def copy() = ???

  private val endInclusive = end.plusDays(1)

  def iterator: Iterator[LocalDate] = Iterator.iterate(start)(_.plusDays(1))
    .takeWhile(_.isBefore(endInclusive))

  override def toString(): String = s"DateRange($start, $end)"

  def inclusiveSize: Long = ChronoUnit.DAYS.between(start, endInclusive)

  override def knownSize: Int = inclusiveSize.toInt
}

object DateRangeInclusive {

  sealed trait CreationError
  object CreationError {
    case object DateRangeBeyondMaxInt extends CreationError
    case object StartIsAfterEnd extends CreationError
  }

  /**
   * []
   * create a date range even if start == end
   *
   * @return None when start is after end
   */
  def mk(start: LocalDate, end: LocalDate): Either[CreationError, DateRangeInclusive] = {
    if (ChronoUnit.DAYS.between(start, end.plusDays(1)) > Int.MaxValue) {
      Left(CreationError.DateRangeBeyondMaxInt)
    } else Either.cond(start.isBefore(end) || start == end, DateRangeInclusive(start, end), CreationError.StartIsAfterEnd)
  }
}


/**
 * []
 * Every 7 days has a Sunday, so that we only need to care about date range < 7 days
 * Within 7 days, if date diff from Sunday to the start is smaller
 * than date range size then the range contains Sunday
 */
def countSundaysInRange(dateRange: DateRangeInclusive): Long = {

  val weekCount = ChronoUnit.WEEKS.between(dateRange.start, dateRange.end)

  val leftOverRange = DateRangeInclusive.mk(dateRange.start.plusWeeks(weekCount), dateRange.end).toOption.get

  val dateDiffToClosestSunday = DayOfWeek.SUNDAY.getValue() - leftOverRange.start.getDayOfWeek().getValue()

  val isLeftOverRangeIncludeSunday = dateDiffToClosestSunday < leftOverRange.inclusiveSize

  weekCount + (if (isLeftOverRangeIncludeSunday) 1 else 0)
}


def countSundaysInRange(start: LocalDate, end: LocalDate): Either[CreationError, Long] = {
  DateRangeInclusive.mk(start, end).map(countSundaysInRange)
}