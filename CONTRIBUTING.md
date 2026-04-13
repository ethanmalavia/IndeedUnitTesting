# Contributing to IndeedUnitTesting

Thank you for your interest in contributing! This project is part of an academic course, but contributions that improve test coverage, fix bugs, or improve documentation are welcome.

## How to Contribute

### 1. Fork and Clone

```bash
git clone https://github.com/ethanmalavia/IndeedUnitTesting.git
cd IndeedUnitTesting
```

### 2. Create a Branch

Use a descriptive branch name:

```bash
git checkout -b feature/add-salary-search-test
```

### 3. Make Your Changes

- Follow the existing code style (test class per feature area, shared setup in `BaseTest`).
- Each test method should be independent and not rely on other tests' state.
- Use `@BeforeMethod` / `@AfterMethod` patterns already established in `BaseTest`.

### 4. Run the Tests

```bash
mvn test
```

All existing tests must pass before submitting.

### 5. Submit a Pull Request

- Target the `main` branch.
- Describe what you changed and why.
- Reference any related issues.

## Code Style

- Java 21, standard Maven project layout.
- Test class names end in `Test`.
- Keep WebDriver waits explicit (`WebDriverWait`) rather than using `Thread.sleep`.

## Reporting Bugs

Open a [GitHub Issue](https://github.com/ethanmalavia/IndeedUnitTesting/issues) with:
- Steps to reproduce
- Expected vs. actual behavior
- Chrome and Java version

## Questions

Open a Discussion or Issue on GitHub.
