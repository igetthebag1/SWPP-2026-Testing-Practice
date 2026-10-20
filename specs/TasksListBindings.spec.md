# Spec: `TasksListBindings.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `@BindingAdapter("app:dueDateLabel") fun setDueDateLabel(textView: TextView, task: Task?)`

**Intent**: Show the task's D-day label (from `dueDateLabel`, using today's date) in the `due_date_text` TextView of a task row, and show nothing when the task has no label. Every call must fully reflect the task it is given: rows are re-bound when a task is checked/unchecked and when RecyclerView recycles a row, so a label from a previous binding must never stay on screen.

"Shows no label" means: the TextView's text is empty **or** the TextView is not visible (`GONE`/`INVISIBLE`). Either is acceptable; showing any old text (e.g. `"CLEAR"`) is not.

**Pre-conditions**
- `textView` is the `due_date_text` TextView of an inflated `task_item` row. It may already show a label from an earlier binding.
- `task` is the task bound to the row, or `null` before binding.
- Today's date is taken from the device clock (`LocalDate.now()`).

**Post-conditions**
- Success cases
  - Active task due in the future: the TextView is visible and shows `"D-N"`. `[Integration]`
    - Tip: Given a repository with Task("TITLE1", "DESC1", isCompleted = false, id = "id1", dueDate = today + 3) / When TasksFragment is launched / Then the `due_date_text` in the row containing "TITLE1" is displayed with text `"D-3"`.
  - Active task due today: the TextView is visible and shows `"D-Day"`. `[Integration]`
    - Tip: Given a repository with Task("TITLE1", "DESC1", false, "id1", dueDate = today) / When TasksFragment is launched / Then the row's `due_date_text` is displayed with text `"D-Day"`.
  - Active overdue task: the TextView is visible and shows `"D+N"`. `[Integration]`
    - Tip: Given a repository with Task("TITLE1", "DESC1", false, "id1", dueDate = today - 2) / When TasksFragment is launched / Then the row's `due_date_text` is displayed with text `"D+2"`.
  - Completed task with a due date: the TextView is visible and shows `"CLEAR"`. `[Integration]`
    - Tip: Given a repository with Task("TITLE1", "DESC1", true, "id1", dueDate = today + 3) / When TasksFragment is launched / Then the row's `due_date_text` is displayed with text `"CLEAR"`.
  - Completed task with no due date: the TextView is visible and shows `"CLEAR"`. `[Integration]`
    - Tip: Given a repository with Task("TITLE1", "DESC1", true, "id1", dueDate = null) / When TasksFragment is launched / Then the row's `due_date_text` is displayed with text `"CLEAR"`.
  - Active task with no due date: no label is shown. `[Integration]`
    - Tip: Given a repository with Task("TITLE1", "DESC1", false, "id1", dueDate = null) / When TasksFragment is launched / Then the row's `due_date_text` shows no label.
  - A `null` task shows no label. `[Integration]`
    - Tip: Given a fresh TextView / When setDueDateLabel(textView, null) is called / Then the TextView shows no label.
- Error cases
  - None. Every `Task?` value, including `null`, is valid input.
- Edge cases
  - Checking an active task with a due date updates the label to `"CLEAR"`. `[Integration]`
    - Tip: Given a repository with Task("TITLE1", "DESC1", false, "id1", dueDate = today + 3) and TasksFragment launched / When the row's `complete_checkbox` is clicked / Then the row's `due_date_text` shows `"CLEAR"`.
  - Unchecking a completed task with a due date restores the D-day label. `[Integration]`
    - Tip: Given a repository with Task("TITLE1", "DESC1", true, "id1", dueDate = today + 3) and TasksFragment launched / When the row's `complete_checkbox` is clicked / Then the row's `due_date_text` shows `"D-3"`.
  - Checking an active task with no due date makes `"CLEAR"` appear. `[Integration]`
    - Tip: Given a repository with Task("TITLE1", "DESC1", false, "id1", dueDate = null) and TasksFragment launched / When the row's `complete_checkbox` is clicked / Then the row's `due_date_text` is displayed with text `"CLEAR"`.
  - Unchecking a completed task with no due date removes `"CLEAR"` (label goes from non-empty to empty). `[Integration]`
    - Tip: Given a repository with Task("TITLE1", "DESC1", true, "id1", dueDate = null) and TasksFragment launched / When the row's `complete_checkbox` is clicked / Then the row's `due_date_text` shows no label.
  - Re-binding a TextView that shows `"CLEAR"` with an active task with no due date clears it (recycled row). `[Integration]`
    - Tip: Given a TextView after setDueDateLabel(textView, Task("T", "D", true, "id1", null)) shows `"CLEAR"` / When setDueDateLabel(textView, Task("T", "D", false, "id1", null)) is called / Then the TextView shows no label.
  - Re-binding a TextView that shows `"CLEAR"` with `null` clears it. `[Integration]`
    - Tip: Given a TextView after setDueDateLabel(textView, Task("T", "D", true, "id1", null)) shows `"CLEAR"` / When setDueDateLabel(textView, null) is called / Then the TextView shows no label.
  - Re-binding a hidden/empty TextView with a task that has a label shows it again (label goes from empty to non-empty). `[Integration]`
    - Tip: Given a TextView after setDueDateLabel(textView, Task("T", "D", false, "id1", null)) shows no label / When setDueDateLabel(textView, Task("T", "D", false, "id1", today + 3)) is called / Then the TextView is visible with text `"D-3"`.

**Undefined behavior** (optional)
- A test running exactly across midnight may see `LocalDate.now()` change between arranging the data and binding the row; tests should not run near midnight.
