# IndeedUnitTesting

[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/ethanmalavia/IndeedUnitTesting/badge)](https://securityscorecards.dev/viewer/?uri=github.com/ethanmalavia/IndeedUnitTesting)
[![OpenSSF Best Practices](https://www.bestpractices.dev/projects/12508/badge)](https://www.bestpractices.dev/projects/12508)

A Selenium + TestNG automated test suite that validates core functionality of [Indeed.com](https://www.indeed.com), including job search, filtering, navigation, and job management features.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Test Suites](#test-suites)
- [Getting Started](#getting-started)
- [Running the Tests](#running-the-tests)
- [Security](#security)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

This project was built as part of a software testing course (CEN 4072) at Florida International University. It demonstrates end-to-end browser automation using Selenium WebDriver, with WebDriverManager handling browser driver setup automatically.

Cloudflare bot-detection bypass is handled in the shared `BaseTest` setup so all tests run cleanly in a headless-compatible Chrome environment.

---

## Tech Stack

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 21 | Primary language |
| Selenium WebDriver | 4.21.0 | Browser automation |
| TestNG | 7.10.2 | Test framework |
| WebDriverManager | 5.9.1 | Automatic driver management |
| Maven | 3.x | Build & dependency management |

---

## Project Structure

```
IndeedUnitTesting/
├── src/
│   └── test/java/org/example/
│       ├── BaseTest.java               # Shared WebDriver setup/teardown
│       ├── HomePageTest.java           # Home page UI validation
│       ├── JobListingTest.java         # Individual job listing checks
│       ├── JobManagementTest.java      # "My Jobs" tab management
│       ├── JobSearchEndToEndTest.java  # Full search-to-apply flow
│       ├── NavigationTabsTest.java     # Top navigation tab traversal
│       ├── SearchFiltersTest.java      # Search filter interactions
│       └── SearchResultsTest.java     # Search results validation
├── testng.xml                          # TestNG suite configuration
├── pom.xml                             # Maven build configuration
├── SECURITY.md                        # Security policy
├── CONTRIBUTING.md                    # Contribution guidelines
└── LICENSE                            # MIT License
```

---

## Test Suites

| Test Class | What It Covers |
|------------|----------------|
| `HomePageTest` | Page title, search bar visibility, logo presence |
| `JobListingTest` | Job card details, apply button, company info |
| `JobManagementTest` | Saved jobs, applied jobs under "My Jobs" tab |
| `JobSearchEndToEndTest` | Full end-to-end: search → filter → click listing → apply |
| `NavigationTabsTest` | Jobs, Company, Salaries, and other top-nav tabs |
| `SearchFiltersTest` | Remote/full-time/part-time filters, date posted, salary |
| `SearchResultsTest` | Result count, pagination, result card structure |

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.6+
- Google Chrome (latest stable)

### Clone

```bash
git clone https://github.com/ethanmalavia/IndeedUnitTesting.git
cd IndeedUnitTesting
```

### Install dependencies

```bash
mvn install -DskipTests
```

---

## Running the Tests

Run the full suite via Maven:

```bash
mvn test
```

Or run a specific test class:

```bash
mvn test -Dtest=HomePageTest
```

TestNG reports are generated in `target/surefire-reports/` after each run.

---

## Security

Please review [SECURITY.md](SECURITY.md) for our vulnerability disclosure policy.

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to contribute to this project.

---

## License

This project is licensed under the [MIT License](LICENSE).
