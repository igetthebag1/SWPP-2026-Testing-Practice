package com.example.swpp.tododapp.tasks

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.swpp.tododapp.R
import com.example.swpp.tododapp.ServiceLocator
import com.example.swpp.tododapp.data.Task
import com.example.swpp.tododapp.data.source.FakeAndroidTestRepository
import com.example.swpp.tododapp.data.source.TasksRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Tests for [setDueDateLabel], one per Tip in specs/TasksListBindings.spec.md.
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class TasksListBindingsTest {

    private lateinit var repository: TasksRepository
    private val today: LocalDate = LocalDate.now()

    @Before
    fun initRepository() {
        repository = FakeAndroidTestRepository()
        ServiceLocator.tasksRepository = repository
    }

    @After
    fun cleanupDb() = runTest {
        ServiceLocator.resetRepository()
    }

    // Helpers

    /** The `due_date_text` in the row whose title is [title]. */
    private fun dueDateTextOf(title: String): Matcher<View> =
        allOf(withId(R.id.due_date_text), hasSibling(withText(title)))

    /** The `complete_checkbox` in the row whose title is [title]. */
    private fun checkboxOf(title: String): Matcher<View> =
        allOf(withId(R.id.complete_checkbox), hasSibling(withText(title)))

    /** "Shows no label": empty text or not displayed (spec definition). */
    private fun showsNoLabel(): Matcher<View> = anyOf(withText(""), not(isDisplayed()))

    private fun launchTasks() {
        launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)
    }

    private fun assertNoLabel(textView: TextView) {
        assertTrue(
            "expected no label but was \"${textView.text}\" (visibility=${textView.visibility})",
            textView.text.isEmpty() || textView.visibility != View.VISIBLE
        )
    }

    private fun newTextView() =
        TextView(InstrumentationRegistry.getInstrumentation().targetContext)

    // Success cases

    @Test
    fun setDueDateLabel_activeDueInThreeDays_showsDMinus3() = runTest {
        // GIVEN - An active task due in 3 days
        repository.saveTask(Task("TITLE1", "DESC1", false, "id1", today.plusDays(3)))

        // WHEN - The task list is shown
        launchTasks()

        // THEN - The row shows D-3
        onView(dueDateTextOf("TITLE1")).check(matches(allOf(isDisplayed(), withText("D-3"))))
    }

    @Test
    fun setDueDateLabel_activeDueToday_showsDDay() = runTest {
        // GIVEN - An active task due today
        repository.saveTask(Task("TITLE1", "DESC1", false, "id1", today))

        // WHEN - The task list is shown
        launchTasks()

        // THEN - The row shows D-Day
        onView(dueDateTextOf("TITLE1")).check(matches(allOf(isDisplayed(), withText("D-Day"))))
    }

    @Test
    fun setDueDateLabel_activeOverdueByTwoDays_showsDPlus2() = runTest {
        // GIVEN - An active task overdue by 2 days
        repository.saveTask(Task("TITLE1", "DESC1", false, "id1", today.minusDays(2)))

        // WHEN - The task list is shown
        launchTasks()

        // THEN - The row shows D+2
        onView(dueDateTextOf("TITLE1")).check(matches(allOf(isDisplayed(), withText("D+2"))))
    }

    @Test
    fun setDueDateLabel_completedWithDueDate_showsClear() = runTest {
        // GIVEN - A completed task with a due date
        repository.saveTask(Task("TITLE1", "DESC1", true, "id1", today.plusDays(3)))

        // WHEN - The task list is shown
        launchTasks()

        // THEN - The row shows CLEAR
        onView(dueDateTextOf("TITLE1")).check(matches(allOf(isDisplayed(), withText("CLEAR"))))
    }

    @Test
    fun setDueDateLabel_completedWithoutDueDate_showsClear() = runTest {
        // GIVEN - A completed task with no due date
        repository.saveTask(Task("TITLE1", "DESC1", true, "id1", null))

        // WHEN - The task list is shown
        launchTasks()

        // THEN - The row shows CLEAR
        onView(dueDateTextOf("TITLE1")).check(matches(allOf(isDisplayed(), withText("CLEAR"))))
    }

    @Test
    fun setDueDateLabel_activeWithoutDueDate_showsNoLabel() = runTest {
        // GIVEN - An active task with no due date
        repository.saveTask(Task("TITLE1", "DESC1", false, "id1", null))

        // WHEN - The task list is shown
        launchTasks()

        // THEN - The row shows no label
        onView(dueDateTextOf("TITLE1")).check(matches(showsNoLabel()))
    }

    @Test
    fun setDueDateLabel_nullTask_showsNoLabel() {
        // GIVEN - A fresh TextView
        val textView = newTextView()

        // WHEN - It is bound with no task
        setDueDateLabel(textView, null)

        // THEN - No label
        assertNoLabel(textView)
    }

    // Edge cases

    @Test
    fun setDueDateLabel_checkActiveWithDueDate_showsClear() = runTest {
        // GIVEN - An active task due in 3 days on the list
        repository.saveTask(Task("TITLE1", "DESC1", false, "id1", today.plusDays(3)))
        launchTasks()

        // WHEN - Its checkbox is clicked
        onView(checkboxOf("TITLE1")).perform(click())

        // THEN - The label becomes CLEAR
        onView(dueDateTextOf("TITLE1")).check(matches(allOf(isDisplayed(), withText("CLEAR"))))
    }

    @Test
    fun setDueDateLabel_uncheckCompletedWithDueDate_showsDMinus3() = runTest {
        // GIVEN - A completed task due in 3 days on the list
        repository.saveTask(Task("TITLE1", "DESC1", true, "id1", today.plusDays(3)))
        launchTasks()

        // WHEN - Its checkbox is clicked
        onView(checkboxOf("TITLE1")).perform(click())

        // THEN - The D-day label comes back
        onView(dueDateTextOf("TITLE1")).check(matches(allOf(isDisplayed(), withText("D-3"))))
    }

    @Test
    fun setDueDateLabel_checkActiveWithoutDueDate_showsClear() = runTest {
        // GIVEN - An active task with no due date on the list
        repository.saveTask(Task("TITLE1", "DESC1", false, "id1", null))
        launchTasks()

        // WHEN - Its checkbox is clicked
        onView(checkboxOf("TITLE1")).perform(click())

        // THEN - CLEAR appears
        onView(dueDateTextOf("TITLE1")).check(matches(allOf(isDisplayed(), withText("CLEAR"))))
    }

    @Test
    fun setDueDateLabel_uncheckCompletedWithoutDueDate_showsNoLabel() = runTest {
        // GIVEN - A completed task with no due date on the list
        repository.saveTask(Task("TITLE1", "DESC1", true, "id1", null))
        launchTasks()

        // WHEN - Its checkbox is clicked
        onView(checkboxOf("TITLE1")).perform(click())

        // THEN - CLEAR is removed
        onView(dueDateTextOf("TITLE1")).check(matches(showsNoLabel()))
    }

    @Test
    fun setDueDateLabel_rebindClearWithActiveNoDueDate_showsNoLabel() {
        // GIVEN - A TextView showing CLEAR
        val textView = newTextView()
        setDueDateLabel(textView, Task("T", "D", true, "id1", null))
        assertEquals("CLEAR", textView.text.toString())

        // WHEN - It is re-bound with an active task with no due date
        setDueDateLabel(textView, Task("T", "D", false, "id1", null))

        // THEN - No label
        assertNoLabel(textView)
    }

    @Test
    fun setDueDateLabel_rebindClearWithNull_showsNoLabel() {
        // GIVEN - A TextView showing CLEAR
        val textView = newTextView()
        setDueDateLabel(textView, Task("T", "D", true, "id1", null))
        assertEquals("CLEAR", textView.text.toString())

        // WHEN - It is re-bound with no task
        setDueDateLabel(textView, null)

        // THEN - No label
        assertNoLabel(textView)
    }

    @Test
    fun setDueDateLabel_rebindEmptyWithDueDate_showsDMinus3() {
        // GIVEN - A TextView showing no label
        val textView = newTextView()
        setDueDateLabel(textView, Task("T", "D", false, "id1", null))

        // WHEN - It is re-bound with a task due in 3 days
        setDueDateLabel(textView, Task("T", "D", false, "id1", today.plusDays(3)))

        // THEN - The label is visible with D-3
        assertEquals(View.VISIBLE, textView.visibility)
        assertEquals("D-3", textView.text.toString())
    }
}
