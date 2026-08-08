# Execution Environment

| Item | Value |
|---|---|
| Operating system | Microsoft Windows 11 Pro (Build 10.0.26200) |
| CPU model | 11th Gen Intel(R) Core(TM) i7-11800H @ 2.30GHz |
| Logical processors | 16 |
| RAM | 32,492 MB (~32 GB) |
| JDK vendor and version | Oracle JDK 26.0.1 (compiled with `--release 21`) |
| Maven version | Apache Maven 3.9.16 |
| Measurement date | 2026-08-07 to 2026-08-08 |

## Notes

- The project targets Java 21 language/bytecode level via `maven.compiler.release=21` in `pom.xml`, even though the installed JDK is a newer version (26.0.1). Virtual threads and all Java 21 features used in this lab are fully supported.
- Measurements were taken with 2 warm-up runs and 5 measured runs per configuration, as required by the lab methodology.
- All ten mandatory configurations (`SEQUENTIAL`, `FIXED` with pool sizes 2/4/8, and `VIRTUAL`, each with and without simulated I/O) were measured on this same machine, to satisfy the requirement of using the same environment and baseline per scenario.
