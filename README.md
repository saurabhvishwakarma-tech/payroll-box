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

## What's next (not built yet, on purpose)

- **Repository<T>**: once generics are learned properly, WorkforceService's Map<Integer, Employee> is a natural thing to generalize.
- **Threaded payroll simulation**: once concurrency is learned, calculating pay for different departments in parallel, plus a look at what actually needs synchronizing (the shared nextEmployeeId counter is the obvious candidate) and why.

These are left out for now rather than added early "because it's good practice." The point of this project is that each part maps to something actually learned, not a checklist of patterns.
