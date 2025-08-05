# Java Calculator

A simple, extensible calculator implemented in Java, supporting basic arithmetic operations, undo/redo functionality, and precision handling using `BigDecimal`. This project demonstrates clean code architecture and unit testing with JUnit.

## Features

- Basic arithmetic operations: addition, subtraction, multiplication, division
- Supports negative and decimal operands
- Undo and redo operations
- Singleton pattern for calculator instance
- Precision handling with `BigDecimal`
- Exception handling for invalid operations and division by zero
- Easily extensible for new operations

## Project Structure

```text
pom.xml
src/
  main/
    java/
      com/
        gordan/
          Calculator.java
          command/
            AbstractCommand.java
            AddCommand.java
            Command.java
            DivideCommand.java
            MultiplyCommand.java
            SubtractCommand.java
          enums/
            Operator.java
  test/
    java/
      com/
        gordan/
          CalculatorTest.java
```

## Prerequisites

- Java 11 or higher
- Maven (for build and dependency management)

## Build & Run

1. Clone the repository:

   ```sh
   git https://github.com/goateeya/amwayhw.git
   cd calculator
   ```

2. Build the project using Maven:

   ```sh
   mvn clean install
   ```

3. Run the application (if you have a main class):

   ```sh
   mvn exec:java -Dexec.mainClass="com.gordan.Calculator"
   ```

## Running Tests

- All unit tests are located in `src/test/java/com/gordan/CalculatorTest.java`.
- To run tests:

  ```sh
  mvn test
  ```

- To add new test cases, create methods in `CalculatorTest.java` using the JUnit `@Test` annotation.

## Contribution

Feel free to fork the repository and submit pull requests. For new features or bug fixes, please:

- Write clear, descriptive commit messages
- Add relevant unit tests in `CalculatorTest.java`
- Ensure all tests pass before submitting

## License

This project is licensed under the MIT License. See the LICENSE file for details.
