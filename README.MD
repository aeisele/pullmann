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

### DD.3 GitHub Rest API

Integration works against the GitHub Rest API to keep things simple.

One major disadvantage is that the Rest response bodies can get quite large and not all the info is needed.
Usage of GraphQL would rectify this problem.

### DD.4 URL construction

The GitHub API gives back complete URLs ones could use to navigate, e.g. from repo to pull requests.
Due to how the application is structured, usage of those URLs from a given DTO to later calls would
necessitate feeding back those URLs into request parameters. This could be problematic because we 
would still need to verify that the URLs are genuine and not malicious. So for the time being URLs
will be constructed as stated in GitHub's API documentation.

## Missing Stuff

### I18N

Strings are hardcoded to US english for now. Both Spring and Thymeleaf would support I18N via resource
bundles though.

### Proper Client Notifications

Currently there are rudimentary _Server Side Events_ to basically trigger a reload of the downloads page
on status changes. Due to all kinds of edge cases regarding browser compatibility / settings this is not
a super solid solution.