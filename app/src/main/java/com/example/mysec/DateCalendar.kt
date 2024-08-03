import java.util.Calendar
import java.util.Date

class DateCalendar(date: Date) {

    companion object {
        const val DAYS_OF_WEEK = 7 // 일주일의 일수
        const val LOW_OF_CALENDAR = 6 // 달력 뷰의 주 수
    }

    val calendar = Calendar.getInstance() // 날짜 작업을 위한 Calendar 인스턴스

    var prevTail = 0 // 현재 달력 뷰에 표시할 이전 달의 날짜 수
    var nextHead = 0 // 현재 달력 뷰에 표시할 다음 달의 날짜 수
    var currentMaxDate = 0 // 현재 달의 최대 일수

    var dateList = arrayListOf<Int>() // 달력에 표시할 날짜를 저장하는 리스트

    init {
        calendar.time = date // 제공된 날짜로 캘린더 초기화
    }

    fun initBaseCalendar() {
        makeMonthDate() // 달력의 날짜를 생성
    }

    private fun makeMonthDate() {
        dateList.clear() // 기존 날짜 목록 초기화

        calendar.set(Calendar.DATE, 1) // 캘린더를 현재 달의 첫째 날로 설정

        currentMaxDate = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) // 현재 달의 최대 일수 가져오기

        prevTail = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 현재 달의 첫째 날의 요일을 통해 이전 달의 남은 날짜 수 계산

        makePrevTail(calendar.clone() as Calendar) // 이전 달의 날짜를 생성하여 추가
        makeCurrentMonth(calendar) // 현재 달의 날짜를 생성하여 추가

        nextHead = LOW_OF_CALENDAR * DAYS_OF_WEEK - (prevTail + currentMaxDate) // 다음 달의 날짜 수 계산
        makeNextHead() // 다음 달의 날짜를 생성하여 추가
    }

    private fun makePrevTail(calendar: Calendar) {
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1) // 전달로 설정
        val maxDate = calendar.getActualMaximum(Calendar.DATE) // 전달의 최대 일수 가져오기
        var maxOffsetDate = maxDate - prevTail // 전달의 남은 날짜 계산

        for (i in 1..prevTail) dateList.add(++maxOffsetDate) // 전달의 날짜를 dateList에 추가
    }

    private fun makeCurrentMonth(calendar: Calendar) {
        for (i in 1..calendar.getActualMaximum(Calendar.DATE)) dateList.add(i) // 현재 달의 날짜를 dateList에 추가
    }

    private fun makeNextHead() {
        var date = 1

        for (i in 1..nextHead) dateList.add(date++) // 다음 달의 날짜를 dateList에 추가
    }

}
