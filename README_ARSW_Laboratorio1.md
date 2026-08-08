# ARSW — Blacklist Concurrency Laboratory

> **Java 21 laboratory on concurrency, performance measurement, fixed thread pools, and virtual threads.**

**Course:** Arquitecturas de Software — ARSW  
**Institution:** Universidad Escuela Colombiana de Ingeniería Julio Garavito  
**Professor:** Javier Iván Toquica  
**Work mode:** Teams of three students 
**Technology:** Java 21 · Maven · JUnit 5  
**Submission deadline:** Defined in the institutional platform (Teams)

---

## 1. Laboratory purpose

This laboratory evaluates the implementation and experimental comparison of three strategies for consulting blacklist providers:

1. Sequential execution.
2. Concurrent execution with a fixed-size thread pool.
3. Concurrent execution with Java 21 virtual threads.

The goal is not merely to identify the fastest implementation. Each team must produce evidence to explain:

- When concurrency improves performance.
- When task coordination introduces more overhead than benefit.
- How blocking operations affect the choice of concurrency model.
- How correctness is preserved when several tasks execute concurrently.
- What architectural trade-offs exist among performance, complexity, scalability, and maintainability.

> **Correctness comes before performance.** A benchmark is invalid when the compared strategies do not produce equivalent results.

---

## 2. Relationship with Workshop 1

Workshop 1 and Laboratory 1 use the same case, but they are different activities.

### Workshop 1

- Inspect the starter project.
- Execute the sequential implementation.
- Analyze architectural decisions, quality attributes, metrics, and trade-offs.
- Do not implement the concurrent solutions.

### Laboratory 1

- Implement the missing concurrent strategies.
- Create automated tests.
- Execute a controlled benchmark.
- Analyze experimental evidence.
- Document and defend the resulting architectural recommendation.

The decision matrix completed during the workshop is **not** a laboratory deliverable. The laboratory grade is based on implementation, correctness, measurement, analysis, and repository evidence.

---

## 3. Problem statement

A system receives an IP address and asks multiple blacklist providers whether that address has been reported.

The starter project creates 100 deterministic providers. A provider can optionally simulate a blocking I/O operation by waiting for a controlled amount of time.

The supplied sequential implementation:

- Consults all providers.
- Collects the identifiers of matching providers.
- Reports the number of consulted providers.
- Measures elapsed time.
- Classifies the IP according to an alarm threshold.

The laboratory must preserve the same functional result while changing the execution strategy.

---

## 4. Starter project

The repository includes the following relevant classes:

```text
src/
├── main/
│   └── java/edu/eci/arsw/blacklist/
│       ├── BenchmarkRunner.java
│       ├── BlackListProvider.java
│       ├── BlackListSearch.java
│       ├── FixedPoolBlackListSearch.java
│       ├── MockBlackListProvider.java
│       ├── ProviderFactory.java
│       ├── SearchResult.java
│       ├── SequentialBlackListSearch.java
│       └── VirtualThreadBlackListSearch.java
└── test/
    └── java/edu/eci/arsw/blacklist/
        └── SequentialBlackListSearchTest.java
```

### Supplied implementation

`SequentialBlackListSearch` is complete and must be used as the functional baseline.

### Pending implementations

The following classes intentionally contain `TODO` work:

- `FixedPoolBlackListSearch`
- `VirtualThreadBlackListSearch`

`BenchmarkRunner` initially executes only the sequential strategy. Each team must extend it to run the required benchmark configurations.

---

## 5. Technical requirements

Before starting, verify:

```bash
java -version
mvn -version
```

Required versions:

- JDK 21.
- Maven 3.9 or later.
- Git.
- A GitHub account.

Compile and execute the supplied baseline:

```bash
mvn clean test
mvn exec:java
```

Execute the baseline with and without simulated I/O:

```bash
mvn exec:java -Dexec.args="202.24.34.55 true"
mvn exec:java -Dexec.args="202.24.34.55 false"
```

The default IP address is:

```text
202.24.34.55
```

---

## 6. Repository setup

Each team must create its own repository from this template.

Suggested repository name:

```text
arsw-blacklist-lab-gXX
```

Example:

```text
arsw-blacklist-lab-g03
```

Before modifying the code:

1. Add the three team members as collaborators.
2. Clone the team repository.
3. Verify Java 21 and Maven.
4. Run `mvn clean test`.
5. Execute the sequential baseline.
6. Create issues or tasks for the work distribution.
7. Record the baseline result in this README.

Every team member must contribute meaningful commits and must understand the complete solution.

### Baseline result (sequential, single run)

| simulateIo | IP | Matches | Consulted | Elapsed |
|---|---|---|---:|---:|
| `true` | 202.24.34.55 | `[10, 23, 36, 49, 62, 75, 88]` | 100 | ~11,012.979 ms |
| `false` | 202.24.34.55 | `[10, 23, 36, 49, 62, 75, 88]` | 100 | ~0.077 ms |

---

# Part A — Concurrent implementation

## 7. Task 1: Fixed-size thread pool

Complete:

```text
FixedPoolBlackListSearch.java
```

The implementation must:

- Implement the `BlackListSearch` interface.
- Receive the provider list and pool size through the constructor.
- Validate that the pool size is greater than zero.
- Use `ExecutorService`.
- Create the executor with `Executors.newFixedThreadPool(poolSize)`.
- Submit provider consultations as concurrent tasks.
- Wait for all submitted tasks.
- Collect each matching provider identifier exactly once.
- Return deterministic results.
- Report the correct number of consulted providers.
- Measure elapsed time with `System.nanoTime()`.
- Close the executor correctly.
- Preserve interruption when an `InterruptedException` occurs.
- Avoid unsafe shared mutable state.

The required pool sizes are:

```text
2, 4, and 8 platform threads
```

### Implementation restrictions

The following approaches do not satisfy this task:

- Replacing the implementation with `parallelStream()`.
- Using the common `ForkJoinPool`.
- Protecting the entire search method with `synchronized`.
- Delegating the search to `SequentialBlackListSearch`.
- Removing or modifying the provider latency to improve results.
- Returning hard-coded matches.

A valid design may use tasks that return their own result and then consolidate those results after calling `Future.get()`.

---

## 8. Task 2: Java 21 virtual threads

Complete:

```text
VirtualThreadBlackListSearch.java
```

The implementation must:

- Implement the `BlackListSearch` interface.
- Use `Executors.newVirtualThreadPerTaskExecutor()`.
- Create one independent task per provider.
- Wait for all tasks to finish.
- Collect each matching provider identifier exactly once.
- Return deterministic results.
- Report the correct number of consulted providers.
- Measure elapsed time with `System.nanoTime()`.
- Close the executor correctly.
- Preserve interruption and provide meaningful error handling.
- Produce a result equivalent to the sequential baseline.

The virtual-thread implementation must not create a manually sized platform-thread pool.

---

## 9. Required result contract

For the mandatory part of this laboratory, all strategies must perform a **complete scan** of the provider list.

For the same IP address and provider configuration:

```text
Sequential result = Fixed-pool result = Virtual-thread result
```

The following values must be equivalent:

- Matching provider identifiers.
- Number of matching providers.
- Trustworthiness classification.
- Number of consulted providers.

Because concurrent tasks can finish in a different order, the returned matching provider identifiers must be ordered before constructing the final `SearchResult`.

For the supplied set of 100 providers:

```text
consultedProviders = 100
```

Early termination at five matches is not part of the mandatory implementation because it changes the amount of evidence collected. It appears only as an optional extension at the end of this document.

---

# Part B — Automated verification

## 10. Task 3: Tests

Add automated tests for the concurrent implementations.

At minimum, the test suite must verify:

1. The sequential implementation is deterministic.
2. A pool of 2 threads returns the same provider identifiers as the sequential baseline.
3. A pool of 4 threads returns the same provider identifiers as the sequential baseline.
4. A pool of 8 threads returns the same provider identifiers as the sequential baseline.
5. The virtual-thread strategy returns the same provider identifiers as the sequential baseline.
6. Every mandatory strategy reports all 100 providers as consulted.
7. Matching provider identifiers contain no duplicates.
8. Matching provider identifiers are returned in ascending order.
9. Creating a fixed-pool search with a non-positive pool size fails with `IllegalArgumentException`.
10. The project passes all tests with simulated I/O disabled.

Run:

```bash
mvn clean test
```

Tests must validate behavior, not execution speed. Do not write tests that fail because one strategy took a few milliseconds more than another.

---

# Part C — Benchmark runner

## 11. Task 4: Extend `BenchmarkRunner`

Modify `BenchmarkRunner` so that it can select the execution strategy from command-line arguments.

Use the following command contract:

```text
<strategy> <ipAddress> <simulateIo> <warmups> <measuredRuns> [poolSize]
```

Accepted strategy values:

```text
SEQUENTIAL
FIXED
VIRTUAL
```

Examples:

```bash
mvn exec:java -Dexec.args="SEQUENTIAL 202.24.34.55 true 2 5"
```

```bash
mvn exec:java -Dexec.args="FIXED 202.24.34.55 true 2 5 4"
```

```bash
mvn exec:java -Dexec.args="VIRTUAL 202.24.34.55 true 2 5"
```

The runner must:

- Validate the arguments.
- Instantiate the selected strategy.
- Execute the requested warm-up runs without including them in the results.
- Execute the requested measured runs.
- Verify that every measured run produces the expected functional result.
- Calculate minimum, maximum, and average elapsed time.
- Print the selected configuration.
- Print individual measured times.
- Print a summary suitable for copying into `results.csv`.

Recommended output fields:

```text
scenario,strategy,pool_size,run,elapsed_ms,matches,consulted_providers
```

Example row:

```text
IO,FIXED,4,1,2845.327,7,100
```

Do not use IDE timestamps or manually measured wall-clock time. Use the elapsed duration returned by the search implementation.

---

# Part D — Experimental comparison

## 12. Task 5: Benchmark methodology

Use the same computer for all measurements.

Before measuring:

- Close unnecessary applications.
- Connect the computer to power when possible.
- Avoid changing the source code between compared runs.
- Run `mvn clean test`.
- Record the execution environment.
- Use two warm-up executions.
- Use five measured executions.

Required experiment matrix:

| Scenario | Strategy | Threads or tasks |
|---|---|---:|
| Local, no simulated I/O | Sequential | 1 |
| Local, no simulated I/O | Fixed pool | 2 |
| Local, no simulated I/O | Fixed pool | 4 |
| Local, no simulated I/O | Fixed pool | 8 |
| Local, no simulated I/O | Virtual threads | 100 tasks |
| Simulated blocking I/O | Sequential | 1 |
| Simulated blocking I/O | Fixed pool | 2 |
| Simulated blocking I/O | Fixed pool | 4 |
| Simulated blocking I/O | Fixed pool | 8 |
| Simulated blocking I/O | Virtual threads | 100 tasks |

### Important interpretation

The scenario without simulated I/O performs a small local calculation. It is useful for observing coordination overhead, but it is not a complete representation of every CPU-bound workload.

The scenario with simulated I/O represents blocking calls such as network, database, or external-service requests.

Do not invent expected times. Performance depends on the execution environment.

---

## 13. Metrics

For every configuration, report:

- Average elapsed time in milliseconds.
- Minimum elapsed time.
- Maximum elapsed time.
- Number of matches.
- Number of consulted providers.
- Speedup relative to the sequential strategy in the same scenario.

Calculate speedup as:

```text
Speedup = sequential average time / strategy average time
```

Interpretation examples:

- `1.00`: no improvement relative to sequential execution.
- Greater than `1.00`: faster than the sequential baseline.
- Less than `1.00`: slower than the sequential baseline.

Do not compare a strategy executed with simulated I/O against a baseline executed without simulated I/O.

---

## 14. Required results table

Complete this table with actual measurements:

| Scenario | Strategy | Pool size | Average ms | Minimum ms | Maximum ms | Speedup | Matches | Consulted |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| No simulated I/O | Sequential | — | 0.020 | 0.011 | 0.037 | 1.00 | 7 | 100 |
| No simulated I/O | Fixed pool | 2 | 0.495 | 0.320 | 0.699 | 0.04 | 7 | 100 |
| No simulated I/O | Fixed pool | 4 | 0.493 | 0.370 | 0.770 | 0.04 | 7 | 100 |
| No simulated I/O | Fixed pool | 8 | 0.719 | 0.582 | 0.960 | 0.03 | 7 | 100 |
| No simulated I/O | Virtual threads | — | Pending | Pending | Pending | Pending | Pending | Pending |
| Simulated I/O | Sequential | — | 10938.269 | 10936.811 | 10940.099 | 1.00 | 7 | 100 |
| Simulated I/O | Fixed pool | 2 | 5504.811 | 5500.189 | 5509.875 | 1.99 | 7 | 100 |
| Simulated I/O | Fixed pool | 4 | 2824.954 | 2821.876 | 2828.271 | 3.87 | 7 | 100 |
| Simulated I/O | Fixed pool | 8 | 1470.691 | 1469.429 | 1471.838 | 7.44 | 7 | 100 |
| Simulated I/O | Virtual threads | — | Pending | Pending | Pending | Pending | Pending | Pending |
| No simulated I/O | Virtual threads | — | 1.116 | 0.599 | 1.585 | 0.02 | 7 | 100 |
| Simulated I/O | Virtual threads | — | 205.896 | 199.829 | 214.377 | 56.82 | 7 | 100 |
Also include the raw measurements in:

```text
results/results.csv
```

Suggested repository location:

```text
results/
├── results.csv
└── environment.md
```

---

# Part E — Analysis and architectural recommendation

## 15. Task 6: Required analysis

Answer every question with evidence from the experiment.

### 15.1 Correctness

1. How did the team verify that the three strategies produce equivalent results?
2. Why can concurrent tasks return matches in a different order?
3. What mechanism or design prevented lost or duplicated matches?
4. Why should performance not be compared before proving functional equivalence?

**Answers:**

1. Equivalence was checked in three ways. First, manually with `jshell`, running `FixedPoolBlackListSearch` with pool sizes 2, 4 and 8 against the same 100 providers and IP used by `SequentialBlackListSearch`, and comparing the matching provider ids by eye (`[10, 23, 36, 49, 62, 75, 88]` in every case). Second, with automated JUnit tests (`FixedPoolBlackListSearchTest`) that build both a sequential and a fixed-pool search over the same provider list and assert that `matchingProviderIds()` is exactly equal. Third, `BenchmarkRunner` itself computes one sequential "reference" result before running any measured iteration, and every measured run is compared against that reference (same matches, same consulted count); if they ever differ, the benchmark stops with an error instead of silently reporting a wrong number.
2. Each provider is consulted by a separate task running on its own thread from the pool. The order in which those threads actually finish depends on how the JVM and the operating system schedule them, and on how long each provider's simulated latency is (recall that `ProviderFactory` assigns a different latency, between roughly 20 and 200 ms, to each provider). A task submitted early can still finish later than one submitted after it, simply because it happened to get a longer simulated wait or less CPU time. So the completion order has no guaranteed relationship with the provider id order.
3. Two things together prevent lost or duplicated matches. First, each task is a `Callable<Boolean>` that only returns its own result through a `Future`; no task ever writes directly into a shared list. Second, the results are only read and consolidated afterwards, sequentially, in the main thread, using the same index to line up each `Future` with its corresponding provider in the original list. Since only one thread (the main thread) ever appends to the `matches` list, and it does so once per provider, there is no possibility of two threads adding the same id twice or overwriting each other. The final `Collections.sort(matches)` then guarantees a fixed, ascending order regardless of the order in which the tasks actually completed.
4. Measuring how fast an implementation runs is meaningless if that implementation is not answering the same question as the baseline. A strategy that skips providers, loses matches, or duplicates them could easily look "faster" while being wrong. Comparing speed before correctness would risk reporting an appealing number for a broken implementation. That is why the lab's own rule states that a benchmark is invalid when the compared strategies do not produce equivalent results — speed only means something once both sides are known to compute the same thing.

### 15.2 Fixed thread pool

5. What changed when the pool increased from 2 to 4 threads?
6. What changed when the pool increased from 4 to 8 threads?
7. Was the improvement proportional to the number of threads? Explain.
8. What costs are introduced by task creation, scheduling, context switching, and result consolidation?
9. What would happen if the pool size were much larger than the available platform threads?

**Answers:**

5. With simulated I/O, going from 2 to 4 threads roughly halved the average time (5504.811 ms → 2824.954 ms). Without simulated I/O, the average stayed about the same (0.495 ms → 0.493 ms) — there was no meaningful waiting to overlap, so adding more workers did not help.
6. With simulated I/O, going from 4 to 8 threads again roughly halved the average time (2824.954 ms → 1470.691 ms). Without simulated I/O, the average actually got worse (0.493 ms → 0.719 ms), because more threads mean more coordination overhead for a workload that is already essentially instantaneous.
7. In the I/O scenario, yes, very close to proportional: the speedup went from 1.99 (pool 2) to 3.87 (pool 4) to 7.44 (pool 8), near the ideal values of 2x, 4x and 8x. This happens because the work is dominated by waiting, not by CPU usage, so each extra thread can overlap another wait almost for free. In the no-I/O scenario the opposite happened: speedup stayed below 1.00 for every pool size (0.03–0.04), meaning the fixed pool was always slower than the sequential version, because there was no waiting time to hide and the overhead of managing threads outweighed the tiny amount of real work.
8. Creating a task means allocating an object and handing it to the executor's internal queue. Scheduling means the JVM and the operating system have to decide which thread runs on which CPU core and when. Context switching happens when there are more runnable threads than available cores, and it costs time to save and restore each thread's state. Result consolidation (looping over the `Future` list, calling `.get()` on each one, sorting the matches) runs sequentially on the main thread after every task has finished, adding a small serial tail to every search. When the simulated latency per provider is large, all of this overhead is negligible next to the waiting time saved. When the latency is close to zero, this overhead becomes the dominant cost, which is exactly what the no-I/O measurements show.
9. If the pool size were much larger than the number of platform threads the machine can actually run at once (this machine has 16 logical processors), the extra threads could not run truly in parallel — the operating system would have to time-slice them, increasing context-switch overhead without adding real parallel capacity. For I/O-bound work like this lab, an oversized pool can still help up to a point, since most threads spend their time sleeping rather than competing for CPU, but the benefit saturates quickly and each extra thread still costs memory (thread stack) and scheduling overhead. For CPU-bound work, an oversized pool brings no benefit at all and only adds overhead, since the CPU cores are already the bottleneck.

### 15.3 Virtual threads

10. In which scenario did virtual threads provide the clearest benefit?
11. Why are virtual threads especially relevant for blocking operations?
12. Why do virtual threads not make local CPU work automatically faster?
13. What trade-offs remain even when virtual threads are lightweight?

### 15.4 Architectural decision

> Pending: best answered once the `VIRTUAL` results are available, since a fair recommendation should compare all three strategies, not just sequential and fixed pool.

14. Which strategy would the team recommend for a system dominated by blocking external calls?
15. Which strategy would the team recommend for a small local workload?
16. Under what conditions would a fixed pool still be preferable?
17. What evidence from the measurements supports the recommendation?
18. What limitations prevent generalizing the conclusion to every production system?

Answers such as “virtual threads are better” or “more threads are faster” are insufficient without conditions and evidence.

---

## 16. Architectural conclusion

Write a team conclusion of 150 to 250 words.

The conclusion must include:

- The dominant workload characteristic.
- The measured evidence.
- The recommended strategy.
- The conditions under which the recommendation is valid.
- At least one trade-off.
- At least one limitation of the experiment.

### Team conclusion

> Replace this text with the team conclusion.

---

## 17. Individual conclusions

Each student must add an individual conclusion of 80 to 120 words.

### Student 1

**Name:** Pending

> Replace this text with the individual conclusion.

### Student 2

**Name:** Pending

> Replace this text with the individual conclusion.

### Student 3

**Name:** Pending

> Replace this text with the individual conclusion.

---

# Part F — Submission

## 18. Required deliverables

The repository must contain:

- Functional sequential baseline.
- Functional fixed-thread-pool implementation.
- Functional virtual-thread implementation.
- Extended `BenchmarkRunner`.
- Automated tests.
- `results/results.csv`.
- `results/environment.md`.
- Completed results table.
- Answers to all analysis questions.
- Team architectural conclusion.
- Three individual conclusions.
- AI-use declaration.
- Meaningful Git history from all team members.

The repository must compile from a clean clone:

```bash
mvn clean test
```

---

## 19. Execution environment

Complete:

| Item | Value |
|---|---|
| Operating system | Microsoft Windows 11 Pro (Build 10.0.26200) |
| CPU model | 11th Gen Intel(R) Core(TM) i7-11800H @ 2.30GHz |
| Logical processors | 16 |
| RAM | 32,492 MB (~32 GB) |
| JDK vendor and version | Oracle JDK 26.0.1 (compiled with `--release 21`) |
| Maven version | Apache Maven 3.9.16 |
| Measurement date | 2026-08-07 |

---

## 20. Team members and contribution evidence

| Student | GitHub username | Main contribution | Relevant commits |
|---|---|---|---|
| Pending | Pending | Pending | Pending |
| Pending | Pending | Pending | Pending |
| Pending | Pending | Pending | Pending |

Each student must have at least two meaningful commits.

Examples of meaningful commits:

```text
Implement fixed thread pool search
Add virtual-thread search strategy
Add equivalence and ordering tests
Extend benchmark runner and CSV output
Document benchmark analysis and trade-offs
```

Formatting-only changes, name changes, or typo corrections do not count as sufficient contribution evidence.

---

## 21. Final submission tag

After verifying the final version:

```bash
git status
mvn clean test
git tag -a lab-1-final -m "Laboratory 1 final submission"
git push origin lab-1-final
```

Submit the repository URL and confirm that the `lab-1-final` tag is available remotely.

---

# Part G — Grading rubric

## 22. Rubric

| Criterion | Weight | Maximum grade |
|---|---:|---:|
| Correctness and equivalence of results | 20% | 1.00 |
| Fixed-pool and virtual-thread implementations | 20% | 1.00 |
| Benchmark methodology and reproducibility | 25% | 1.25 |
| Analysis and architectural trade-offs | 25% | 1.25 |
| Repository, documentation, and individual traceability | 10% | 0.50 |
| **Total** | **100%** | **5.00** |

### 22.1 Correctness and equivalence — 1.00

Full credit requires:

- All strategies return equivalent matches.
- All mandatory strategies consult 100 providers.
- Results contain no duplicates.
- Results are deterministic and ordered.
- Automated tests pass.

### 22.2 Concurrent implementations — 1.00

Full credit requires:

- Correct use of a fixed `ExecutorService`.
- Correct use of Java 21 virtual threads.
- Proper executor lifecycle.
- Appropriate exception and interruption handling.
- No unsafe global state.
- No sequential delegation disguised as concurrency.

### 22.3 Benchmark methodology — 1.25

Full credit requires:

- All ten mandatory configurations.
- Two warm-ups and five measured executions.
- Same environment and baseline per scenario.
- Raw data and summary metrics.
- Reproducible commands.
- Correct speedup calculations.

### 22.4 Analysis and trade-offs — 1.25

Full credit requires:

- Evidence-based interpretation.
- Correct distinction between blocking and local work.
- Analysis of pool size.
- Analysis of virtual threads.
- Architectural recommendation with conditions.
- Explicit limitations and trade-offs.

### 22.5 Repository and traceability — 0.50

Full credit requires:

- Clear documentation.
- Clean repository structure.
- Meaningful contributions from all students.
- Complete AI-use declaration.
- Final submission tag.
- Successful execution from a clean clone.

---

## 23. Oral verification

Any team member may be selected to:

- Explain a section of the concurrent implementation.
- Describe how race conditions were avoided.
- Explain a benchmark result.
- Reproduce a command.
- Justify the architectural recommendation.
- Explain code produced or modified with AI assistance.

The individual grade may be adjusted when a student cannot demonstrate understanding or contribution.

---

## 24. Use of artificial intelligence

AI tools may be used as support, but every student must understand and defend the submitted work.

Complete the following table:

| Tool | Purpose | Main prompts or activities | Validation performed | Changes made by the team |
|---|---|---|---|---|
| Pending | Pending | Pending | Pending | Pending |

Requirements:

- Do not submit code that the team cannot explain.
- Validate generated code through tests and review.
- Record relevant AI assistance.
- Do not use AI output as a replacement for experimental evidence.
- Plagiarism or duplicated repository content is subject to the course academic-integrity rules.

---

# Optional extensions

These extensions do not replace any mandatory requirement.

## A. Early termination

Create a separate strategy that stops after finding five matches.

Analyze:

- Whether the final classification remains valid.
- Whether the complete evidence list is preserved.
- How pending tasks are cancelled.
- How many providers are actually consulted.
- What happens to tasks already running.
- How early termination changes comparability with the complete-scan benchmark.

Do not replace the mandatory complete-scan strategies with this extension.

## B. Five-minute cache

Add a cache with a five-minute TTL.

Analyze:

- Cache key.
- Thread safety.
- Expiration.
- Stale information.
- Cache hit ratio.
- Effect on elapsed time.
- Effect on correctness and freshness.

---

# Final checklist

Before submission, verify:

- [ ] The project uses Java 21.
- [ ] `mvn clean test` passes.
- [ ] Fixed pools of 2, 4, and 8 threads work.
- [ ] The virtual-thread strategy works.
- [ ] All mandatory strategies return equivalent results.
- [ ] Results are ordered and contain no duplicates.
- [ ] The benchmark runner supports the required arguments.
- [ ] Two warm-ups and five measured runs were executed.
- [ ] All ten required configurations were measured.
- [ ] `results/results.csv` contains raw measurements.
- [ ] The environment is documented.
- [ ] The results table is complete.
- [ ] All analysis questions are answered.
- [ ] The team conclusion is complete.
- [ ] Every student added an individual conclusion.
- [ ] Every student has meaningful commits.
- [ ] AI use is declared.
- [ ] The `lab-1-final` tag was pushed.
- [ ] The repository URL was submitted in the institutional platform.
