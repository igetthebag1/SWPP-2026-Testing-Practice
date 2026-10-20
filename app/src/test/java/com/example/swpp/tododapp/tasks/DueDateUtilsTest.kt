package com.example.swpp.tododapp.tasks

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Tests for [dueDateLabel], one per Tip in specs/DueDateUtils.spec.md.
 */
class DueDateUtilsTest {

    private val today = LocalDate.of(2026, 10, 20)

    // Success cases

    @Test
    fun dueDateLabel_completedWithDueDate_returnsClear() {
        // GIVEN - A completed task due in 5 days
        val dueDate = LocalDate.of(2026, 10, 25)

        // WHEN - The label is built
        val label = dueDateLabel(dueDate, today, isCompleted = true)

        // THEN - It is CLEAR
        assertEquals("CLEAR", label)
    }

    @Test
    fun dueDateLabel_completedWithoutDueDate_returnsClear() {
        // GIVEN - A completed task with no due date
        // WHEN - The label is built
        val label = dueDateLabel(null, today, isCompleted = true)

        // THEN - It is CLEAR
        assertEquals("CLEAR", label)
    }

    @Test
    fun dueDateLabel_activeWithoutDueDate_returnsEmpty() {
        // GIVEN - An active task with no due date
        // WHEN - The label is built
        val label = dueDateLabel(null, today, isCompleted = false)

        // THEN - No label
        assertEquals("", label)
    }

    @Test
    fun dueDateLabel_activeDueInFiveDays_returnsDMinus5() {
        // GIVEN - An active task due 2026-10-25
        val dueDate = LocalDate.of(2026, 10, 25)

        // WHEN - The label is built on 2026-10-20
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - 5 days remain
        assertEquals("D-5", label)
    }

    @Test
    fun dueDateLabel_activeDueToday_returnsDDay() {
        // GIVEN - An active task due today
        // WHEN - The label is built
        val label = dueDateLabel(today, today, isCompleted = false)

        // THEN - It is D-Day
        assertEquals("D-Day", label)
    }

    @Test
    fun dueDateLabel_activeOverdueByThreeDays_returnsDPlus3() {
        // GIVEN - An active task due 2026-10-17
        val dueDate = LocalDate.of(2026, 10, 17)

        // WHEN - The label is built on 2026-10-20
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - 3 days overdue
        assertEquals("D+3", label)
    }

    // Edge cases

    @Test
    fun dueDateLabel_activeDueTomorrow_returnsDMinus1() {
        // GIVEN - An active task due 2026-10-21
        val dueDate = LocalDate.of(2026, 10, 21)

        // WHEN - The label is built on 2026-10-20
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - 1 day remains
        assertEquals("D-1", label)
    }

    @Test
    fun dueDateLabel_activeDueYesterday_returnsDPlus1() {
        // GIVEN - An active task due 2026-10-19
        val dueDate = LocalDate.of(2026, 10, 19)

        // WHEN - The label is built on 2026-10-20
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - 1 day overdue
        assertEquals("D+1", label)
    }

    @Test
    fun dueDateLabel_completedDueToday_returnsClear() {
        // GIVEN - A completed task due today
        // WHEN - The label is built
        val label = dueDateLabel(today, today, isCompleted = true)

        // THEN - CLEAR wins over D-Day
        assertEquals("CLEAR", label)
    }

    @Test
    fun dueDateLabel_acrossMonthEnd_returnsDMinus3() {
        // GIVEN - Today 2026-10-30, due 2026-11-02
        val label = dueDateLabel(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 10, 30), isCompleted = false)

        // THEN - 3 calendar days
        assertEquals("D-3", label)
    }

    @Test
    fun dueDateLabel_acrossYearEnd_returnsDMinus1() {
        // GIVEN - Today 2026-12-31, due 2027-01-01
        val label = dueDateLabel(LocalDate.of(2027, 1, 1), LocalDate.of(2026, 12, 31), isCompleted = false)

        // THEN - 1 calendar day
        assertEquals("D-1", label)
    }

    @Test
    fun dueDateLabel_acrossLeapDay_returnsDMinus2() {
        // GIVEN - Today 2028-02-28, due 2028-03-01 (2028-02-29 in between)
        val label = dueDateLabel(LocalDate.of(2028, 3, 1), LocalDate.of(2028, 2, 28), isCompleted = false)

        // THEN - 2 calendar days
        assertEquals("D-2", label)
    }

    @Test
    fun dueDateLabel_oneDayShortOfOneMonth_returnsDMinus30() {
        // GIVEN - Today 2026-10-20, due 2026-11-19
        val label = dueDateLabel(LocalDate.of(2026, 11, 19), today, isCompleted = false)

        // THEN - 30 calendar days
        assertEquals("D-30", label)
    }

    @Test
    fun dueDateLabel_exactlyOneMonthAway_returnsDMinus31() {
        // GIVEN - Today 2026-10-20, due 2026-11-20
        val label = dueDateLabel(LocalDate.of(2026, 11, 20), today, isCompleted = false)

        // THEN - 31 calendar days, not D-Day
        assertEquals("D-31", label)
    }

    @Test
    fun dueDateLabel_moreThanOneMonthAway_returnsDMinus36() {
        // GIVEN - Today 2026-10-20, due 2026-11-25
        val label = dueDateLabel(LocalDate.of(2026, 11, 25), today, isCompleted = false)

        // THEN - 36 calendar days
        assertEquals("D-36", label)
    }

    @Test
    fun dueDateLabel_moreThanOneYearAway_returnsDMinus366() {
        // GIVEN - Today 2026-10-20, due 2027-10-21
        val label = dueDateLabel(LocalDate.of(2027, 10, 21), today, isCompleted = false)

        // THEN - 366 calendar days
        assertEquals("D-366", label)
    }

    @Test
    fun dueDateLabel_overdueByExactlyOneMonth_returnsDPlus30() {
        // GIVEN - Today 2026-10-20, due 2026-09-20
        val label = dueDateLabel(LocalDate.of(2026, 9, 20), today, isCompleted = false)

        // THEN - 30 days overdue, not D-Day
        assertEquals("D+30", label)
    }

    @Test
    fun dueDateLabel_overdueByMoreThanOneMonth_returnsDPlus35() {
        // GIVEN - Today 2026-10-20, due 2026-09-15
        val label = dueDateLabel(LocalDate.of(2026, 9, 15), today, isCompleted = false)

        // THEN - 35 days overdue
        assertEquals("D+35", label)
    }
}
