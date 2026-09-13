# Payroll Box - Employee & Payroll Management System

A small Employee & Payroll Management System written in core Java only. No Spring, no Maven, no database, no third-party libraries. Everything, including persistence, is built on the standard library: java.io, java.nio.file, java.time, java.util, java.util.stream.

This isn't meant to be a finished, one-and-done project. It's something I keep coming back to as I revise core Java topics. Right now it covers OOP (inheritance, polymorphism, abstraction, interfaces), collections, custom exceptions, streams, and hand-rolled file I/O. Generics and concurrency are left out for now on purpose, see "What's next" below, rather than being bolted on early just because they're "good practice."

## Requirements

- JDK 21+
- No build tool needed. Plain javac/java, or open the src folder as a source root in IntelliJ IDEA.

## Running it

From the project root:

```
javac -d out $(find src -name "*.java")
java -cp out com.payroll.Main
```

Or in IntelliJ: mark src as the Sources Root, then run com.payroll.Main. The app starts with three sample employees already loaded so the reports and sorting have something to show right away.

## Package structure

```
com.payroll.model       Employee hierarchy, Payable interface, Department/EmployeeStatus enums
com.payroll.exception   Custom checked exceptions
com.payroll.service     WorkforceService (CRUD + sorting), ReportingService (streams)
com.payroll.io          CSV export/import
com.payroll.Main        CLI entry point
```

## What each part demonstrates

**com.payroll.model** covers inheritance, polymorphism, abstraction, interfaces and enums. Employee is an abstract class holding everything every employee has in common: id, name, department, join date, base salary, status. Manager, Developer and Intern each override calculatePay() with genuinely different logic, not just cosmetic differences. That's the polymorphism part: code that holds an Employee reference and calls calculatePay() doesn't need to know which subclass it actually is. Employee also implements Comparable<Employee>, with tenure as its natural order.

**com.payroll.exception** has four checked exceptions (DuplicateEmployeeIdException, EmployeeNotFoundException, InvalidSalaryException, MalformedRecordException), each thrown right where the problem is found and caught somewhere that can actually do something about it, instead of just printed and forgotten.

**com.payroll.service** has WorkforceService, the in-memory store, handling add/remove/find/list plus two sorting methods: one through Comparable (tenure, the class's own natural order) and one through Comparator (salary, an ordering that belongs to the use case rather than the class). ReportingService is where the streams live: department totals and averages using groupingBy with a downstream collector, top N earners overall and per department, grouping by status and by tenure bracket, and one report that chains filter, sorted, limit and collect together in a single pipeline.

**com.payroll.io** has CsvEmployeeExporter and CsvEmployeeImporter, reading and writing plain CSV by hand with String.split, no library involved. Every row is validated on its own during import, so one bad row gets logged with its line number and reason while the rest of the file still comes in fine.

**com.payroll.Main** is a Scanner-based menu. All input goes through small readInt/readDouble/readDate/readNonBlankLine helpers that just keep asking until they get something valid, so bad input never crashes the program.

## A few design decisions worth explaining

**Why Employee is an abstract class and not just an interface.** Every employee genuinely shares state (id, name, department, join date, base salary, status) and shared behavior too, like getTenureInYears() and toString(). An interface can't hold that state or provide a shared implementation, it can only declare a contract. Payable exists as a separate interface for the one piece of behavior, calculating pay, that could plausibly apply to something other than an Employee down the line. That's exactly the kind of thing an interface is for and an abstract class isn't.

**Why the id generator needs to know about CSV import.** Ids are auto-generated, not typed in by hand, but a CSV round trip (export, then import that same file back in) has to keep the original ids or every re-import would quietly renumber the whole workforce. So Employee has a second, protected constructor that takes an explicit id. Only CsvEmployeeImporter uses it, and every time it's used the id counter gets bumped past that id if needed. That way an employee added fresh through the CLI afterward can never collide with one just loaded back in from a file.

**Why the CSV parsing is hand-rolled instead of "more robust."** String.split(",", -1) doesn't understand quoted fields, so a name with a literal comma in it, like "Smith, Jane", would silently split into the wrong number of columns. That's a real limitation and a proper CSV parser would handle it. It's left this way on purpose, since the point of this phase is practicing manual parsing and validation, not reimplementing a CSV spec.

**Why a Map instead of a List for the workforce store.** Almost everything WorkforceService does (add, remove, find) is "do something to the employee with this id." A Map answers that instantly. A List would mean scanning every entry on every lookup. LinkedHashMap specifically, so listAll() comes back in the order things were added instead of random hash order.

**Why checked exceptions instead of unchecked.** All four custom exceptions extend Exception, not RuntimeException, on purpose. That forces every caller to actually decide what to do about a duplicate id or an invalid salary, instead of letting it silently bubble up until something crashes. It's more ceremony than unchecked exceptions would be, but the ceremony is the point of this phase.

## Phase 6 (stretch)

**com.payroll.service.Repository<T>** is a generic, in-memory store keyed by an integer id, built from a `Function<T, Integer>` that knows how to pull the id out of whatever T is. WorkforceService no longer stores employees itself, it holds a `Repository<Employee>` underneath and just translates a failed `add`/`remove`/`findById` into the same `DuplicateEmployeeIdException`/`EmployeeNotFoundException` as before. Every method signature on WorkforceService stayed the same, so nothing else in the project (Main, ReportingService, the CSV classes) needed to change at all.

**com.payroll.service.ParallelPayrollCalculator** calculates total pay per department using one thread per department instead of one after another, via ExecutorService, Callable and Future. Each thread only reads employee data and hands its own answer back through a Future, nothing is shared and written to by more than one thread at once, which is what makes this safe without any synchronized blocks. The comments on that class walk through what would actually need synchronizing if it were written differently (a shared results map written to directly by multiple threads, or Employee's static id counter if employees were ever created from more than one thread). Reachable from the CLI under Reports, option 8.

## What's still not built (on purpose)

Nothing major left from the original plan. Anything added from here on would be extending the project for its own sake, which is exactly what it's for.
