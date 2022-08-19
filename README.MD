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
  - Template based frontend instead of JS-based SPA + API
  - no reactive paradigms

### DD.2 _OkHttp_ as HTTP Client

Spring _RestTemplate_ is out of the picture as it is practically deprecated in favor of _WebClient_.
The latter won't be used as it only supports reactive interactions which are ruled out by _DD.1_.

The JVM 11 `java.net.http` client would be a pragmatic first choice but it doesn't provide all the necessary
features like preemptive authentication.

In a bigger project integration of _contract-based_ techniques like _Feign_ and/or _OpenAPI_ would be
chosen to enhance maintainability.

## Missing Stuff

### I18N

Strings are hardcoded to US english for now. Both Spring and Thymeleaf would support I18N via resource
bundles though.

## Abbreviations

- CVC: _collaborative version control_