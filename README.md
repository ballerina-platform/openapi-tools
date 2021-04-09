# Ballerina-OpenAPI
 [![Build master](https://github.com/ballerina-platform/ballerina-openapi/workflows/Build%20master/badge.svg?branch=master)](https://github.com/ballerina-platform/ballerina-openapi/actions?query=workflow%3ABuild)
 [![Daily build](https://github.com/ballerina-platform/ballerina-openapi/workflows/Daily%20build/badge.svg)](https://github.com/ballerina-platform/ballerina-openapi/actions?query=workflow%3A%22Daily+build%22)
 [![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/ballerina-openapi.svg)](https://github.com/ballerina-platform/ballerina-openapi/commits/master)
 [![codecov](https://codecov.io/gh/ballerina-platform/ballerina-openapi/branch/master/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/ballerina-openapi)
 
The OpenAPI Specification is a specification, which creates a RESTFUL contract for APIs detailing all of its resources 
and operations in both human and machine-readable format for easy development, discovery, and integration. Ballerina
 OpenAPI tooling will make it easy for users to start the development of a service documented in the OpenAPI contract 
  by generating the Ballerina service and client skeletons. The OpenAPI tools provide the following capabilities.
 
 1. Generate the Ballerina service or client code for a given OpenAPI definition. 
 2. Export the OpenAPI definition of a Ballerina service.
 3. Validate the service implementation of a given OpenAPI contract.
    
The `openapi` command in Ballerina is used for OpenAPI to Ballerina and Ballerina to OpenAPI code generations. 
The OpenAPI compiler plugin will allow you to validate a service implementation against an OpenAPI contract during
 compile time. 
This plugin ensures that the implementation of a service does not deviate from its OpenAPI contract.
For more information on the supported operations, go to [Using the OpenAPI Tools](https://ballerina.io/swan-lake/learn/using-the-openapi-tools/)
## Building from the Source

### Setting Up the Prerequisites

1. OpenJDK 11 ([Adopt OpenJDK](https://adoptopenjdk.net/) or any other OpenJDK distribution)

    >**Info:** You can also use [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html). Set the JAVA_HOME environment variable to the pathname of the directory into which you installed JDK.

     
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

You can also check for [open issues](https://github.com/ballerina-platform/ballerina-openapi/issues) that
 interest you. We look forward to receiving your contributions.

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of Conduct

All contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful Links

* Discuss about code changes of the Ballerina project in [ballerina-dev@googlegroups.com](mailto:ballerina-dev@googlegroups.com).
* Chat live with us via our [Slack channel](https://ballerina.io/community/slack/).
* Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
* View the [Ballerina performance test results](performance/benchmarks/summary.md).

