# Spec: `DueDateUtils.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `internal fun dueDateLabel(dueDate: LocalDate?, today: LocalDate, isCompleted: Boolean): String`

**Intent**: Build the D-day label shown next to a task in the task list. A completed task shows `CLEAR`; an active task shows how many calendar days remain until (or have passed since) its due date, or nothing if it has no due date. N is the **total** number of calendar days between `today` and `dueDate` (e.g. 36 days, never "1 month 5 days" reduced to 5), so it stays correct across month ends, year ends and leap days.

**Pre-conditions**
- `today` is a valid, non-null `LocalDate` supplied by the caller (the function does not read the system clock).
- `dueDate` is either `null` (no due date) or a valid `LocalDate`.

**Post-conditions**
- Success cases
  - Completed task with a due date returns `"CLEAR"`. `[Unit]`
    - Tip: Given dueDate = 2026-10-25, today = 2026-10-20, isCompleted = true / When dueDateLabel is called / Then it returns `"CLEAR"`.
  - Completed task with no due date returns `"CLEAR"`. `[Unit]`
    - Tip: Given dueDate = null, today = 2026-10-20, isCompleted = true / When dueDateLabel is called / Then it returns `"CLEAR"`.
  - Active task with no due date returns `""` (no label). `[Unit]`
    - Tip: Given dueDate = null, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns `""`.
  - Active task due in N days (N > 0) returns `"D-N"`. `[Unit]`
    - Tip: Given dueDate = 2026-10-25, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns `"D-5"`.
  - Active task due today returns `"D-Day"`. `[Unit]`
    - Tip: Given dueDate = 2026-10-20, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns `"D-Day"`.
  - Active task overdue by N days (N > 0) returns `"D+N"`. `[Unit]`
    - Tip: Given dueDate = 2026-10-17, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns `"D+3"`.
- Error cases
  - None. Every combination of inputs that satisfies the pre-conditions is valid.
- Edge cases
  - Due tomorrow (right next to today, future side) returns `"D-1"`. `[Unit]`
    - Tip: Given dueDate = 2026-10-21, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns `"D-1"`.
  - Due yesterday (right next to today, past side) returns `"D+1"`. `[Unit]`
    - Tip: Given dueDate = 2026-10-19, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns `"D+1"`.
  - Completed task due today returns `"CLEAR"`, not `"D-Day"`. `[Unit]`
    - Tip: Given dueDate = 2026-10-20, today = 2026-10-20, isCompleted = true / When dueDateLabel is called / Then it returns `"CLEAR"`.
  - Due date across a month end returns the calendar-day count. `[Unit]`
    - Tip: Given dueDate = 2026-11-02, today = 2026-10-30, isCompleted = false / When dueDateLabel is called / Then it returns `"D-3"`.
  - Due date across a year end returns the calendar-day count. `[Unit]`
    - Tip: Given dueDate = 2027-01-01, today = 2026-12-31, isCompleted = false / When dueDateLabel is called / Then it returns `"D-1"`.
  - Due date across a leap day returns the calendar-day count. `[Unit]`
    - Tip: Given dueDate = 2028-03-01, today = 2028-02-28, isCompleted = false / When dueDateLabel is called / Then it returns `"D-2"`.
  - Due date one day short of a full month away returns the total day count. `[Unit]`
    - Tip: Given dueDate = 2026-11-19, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns `"D-30"`.
  - Due date exactly one month away returns the total day count, not 0. `[Unit]`
    - Tip: Given dueDate = 2026-11-20, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns `"D-31"`.
  - Due date more than one month away returns the total day count. `[Unit]`
    - Tip: Given dueDate = 2026-11-25, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns `"D-36"`.
  - Due date more than one year away returns the total day count. `[Unit]`
    - Tip: Given dueDate = 2027-10-21, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns `"D-366"`.
  - Overdue by exactly one month returns the total day count, not `"D-Day"`. `[Unit]`
    - Tip: Given dueDate = 2026-09-20, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns `"D+30"`.
  - Overdue by more than one month returns the total day count. `[Unit]`
    - Tip: Given dueDate = 2026-09-15, today = 2026-10-20, isCompleted = false / When dueDateLabel is called / Then it returns `"D+35"`.

**Undefined behavior** (optional)
- None.
