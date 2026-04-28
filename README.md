# Whole-Program Analyzers for EO

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![mvn](https://github.com/objectionary/wpa/actions/workflows/mvn.yml/badge.svg)](https://github.com/objectionary/wpa/actions/workflows/mvn.yml)
[![PDD status](https://www.0pdd.com/svg?name=objectionary/wpa)](https://www.0pdd.com/p?name=objectionary/wpa)
[![Maven Central](https://img.shields.io/maven-central/v/org.eolang/wpa.svg)](https://maven-badges.herokuapp.com/maven-central/org.eolang/wpa)
[![Javadoc](https://www.javadoc.io/badge/org.eolang/wpa.svg)](https://www.javadoc.io/doc/org.eolang/wpa)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/objectionary/wpa/blob/master/LICENSE.txt)

This Java package is a collection of whole-program analyzers
(a.k.a. WPA lints) for [XMIR] — an intermediate representation of
[EO] objects. WPA lints analyze a set of XMIR files together,
rather than one file at a time.

Single-file lints live in a separate package,
[`org.eolang:lints`](https://github.com/objectionary/lints),
which this library builds upon.

Add it to your project:

```xml
<dependency>
  <groupId>org.eolang</groupId>
  <artifactId>wpa</artifactId>
  <version></version>
</dependency>
```

Then, run a whole-program analysis of XMIR files using the `Program`
class:

```java
import java.nio.file.Paths;
import org.eolang.lints.Program;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class Foo {
    @Test
    void testProgram() {
        Assertions.assertTrue(
            new Program(
                Paths.get("xmir-files")
            ).defects().isEmpty()
        );
    }
}
```

You can disable any particular linter with the help of the `+unlint`
meta in your XMIR source.

## Design of This Library

The library is designed as a set of `Lint<Map<String, XML>>`
implementations. The `Program` class is the public entry point —
it discovers `.xmir` files in a directory, runs all WPA lints, and
returns the collected defects.

Classes exposed to users of the library:

* `Program` — checker of a set of [XMIR]
* `Defect` — a single defect discovered (from `org.eolang:lints`)
* `Severity` — severity of a defect (from `org.eolang:lints`)

## How to Contribute

Fork the repository, make changes, and send us a pull request.
We will review your changes and apply them to the `master` branch
shortly, provided they don't violate our quality standards.
To avoid frustration, before sending us your pull request please
run a full Maven build:

```bash
mvn clean install -Pqulice
```

You will need Maven 3.8+ and Java 11+.

[XMIR]: https://news.eolang.org/2022-11-25-xmir-guide.html
[EO]: https://www.eolang.org
