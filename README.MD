# Pull Man
a _Pull Request_ manager

## Build
TODO

## Configuration
TODO

## Design Decisions

### DD.1 usage of _boring stack_

For the sake of simplicity and to not waste effort on unrelated problems, tech choices were made in favor of
familiar options unless otherwise necessary.

Amongst others this means:
  - Java instead of Kotlin
  - Maven instead of Gradle
  - Spring Boot instead of dagger2 / guice
  - Template based frontend instead of JS-based SPA

## Missing Stuff

### I18N

Strings are hardcoded to US english for now. Both Spring and Thymeleaf would support I18N via resource
bundles though.