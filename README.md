# Ballerina-OpenAPI
 [![Build](https://github.com/ballerina-platform/openapi-tools/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/ballerina-platform/openapi-tools/actions/workflows/build-timestamped-master.yml)
 [![codecov](https://codecov.io/gh/ballerina-platform/openapi-tools/branch/master/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/openapi-tools)
 [![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/openapi-tools.svg)](https://github.com/ballerina-platform/openapi-tools/commits/master)
 [![GitHub issues](https://img.shields.io/github/issues/ballerina-platform/ballerina-standard-library/module/openapi-tools.svg?label=Open%20Issues)](https://github.com/ballerina-platform/ballerina-library/labels/module%2Fopenapi-tools)
 
Ballerina OpenAPI tools provide seamless integration between Ballerina and OpenAPI specifications, enabling both 
contract-first and code-first API development approaches. The `bal openapi` command offers contract optimization 
through `flatten` and `align` operations to make OpenAPI contracts Ballerina-compatible, bidirectional code generation
to create Ballerina services and clients from OpenAPI contracts or export OpenAPI specifications from Ballerina 
services, and build-time integration via the `add` command for automated client generation. Additionally, 
the OpenAPI module provides annotations like `@openapi:ServiceInfo`, `@openapi:ResourceInfo`, and `@openapi:Example`
to enrich generated contracts with metadata, enable runtime introspection by exposing OpenAPI documentation, validate
service implementations against existing contracts at compile-time, and enhance API documentation with examples and 
descriptions.

## Building from the Source

### Setting Up the Prerequisites

1. OpenJDK 21 ([Adopt OpenJDK](https://adoptopenjdk.net/) or any other OpenJDK distribution)

    >**Info:** You can also use [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html). Set the JAVA_HOME environment variable to the pathname of the directory into which you installed JDK.
   
2. Export GitHub Personal access token with read package permissions as follows,
   ```
   export packageUser=<Username>
   export packagePAT=<Personal access token>
   ```
     
### Building the Source

Execute the commands below to build from the source.

1. To build the library:
        
        ./gradlew clean build

2. To run the integration tests:

        ./gradlew clean test

3. To build the module without the tests:

        ./gradlew clean build -x test

4. To publish to maven local:

        ./gradlew clean build publishToMavenLocal

## Contributing to Ballerina

As an open-source project, Ballerina welcomes contributions from the community. 

You can also check for [open issues](https://github.com/ballerina-platform/openapi-tools/issues) that
 interest you. We look forward to receiving your contributions.

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of Conduct

All contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful Links

* Discuss about code changes of the Ballerina project in [ballerina-dev@googlegroups.com](mailto:ballerina-dev@googlegroups.com).
* Chat live with us via our [Discord server](https://discord.gg/ballerinalang).
* Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
* View the [Ballerina performance test results](https://github.com/ballerina-platform/ballerina-lang/blob/master/performance/benchmarks/summary.md).

