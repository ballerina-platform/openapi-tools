# Ballerina-OpenAPI
 [![Build master](https://github.com/ballerina-platform/ballerina-openapi/workflows/Build%20master/badge.svg?branch=master)](https://github.com/ballerina-platform/ballerina-openapi/actions?query=workflow%3ABuild)
 [![Daily build](https://github.com/ballerina-platform/ballerina-openapi/workflows/Daily%20build/badge.svg)](https://github.com/ballerina-platform/ballerina-openapi/actions?query=workflow%3A%22Daily+build%22)
 [![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/ballerina-openapi.svg)](https://github.com/ballerina-platform/ballerina-openapi/commits/master)
 
The OpenAPI Specification is a specification, which creates a RESTFUL contract for APIs detailing all of its resources 
and operations in a human and machine readable format for easy development, discovery, and integration. Ballerina
 OpenAPI tooling will make it easy for users to start development of a service documented in the OpenAPI contract 
  by generating the Ballerina service and client skeletons. The OpenAPI tools provide the following capabilities.
 
 1. Generate the Ballerina service or client code for a given OpenAPI definition. 
 2. Export the OpenAPI definition of a Ballerina service.
 3. Validate service implementation of a given OpenAPI Contract.
    
The `openapi` command in Ballerina is used for OpenAPI to Ballerina and Ballerina to OpenAPI code generations. 
The OpenAPI compiler plugin will allow you to validate a service implementation against an OpenAPI contract during
 compile time. 
This plugin ensures that the implementation of a service does not deviate from its OpenAPI contract.
#### OpenAPI to Ballerina
##### Generate Service and Client stub from OpenAPI Contract

```bash
ballerina openapi   -i <openapi-contract> 
               [--service-name: generated files name]
               [--tags: tags list]
               [--operations: operationsID list]
               [--mode service|client ]
               [(-o|--output): output file path]
```
Generates both the Ballerina service and Ballerina client stub for a given OpenAPI file.

This `-i <openapi-contract>` parameter of the command is mandatory. As an input, it will take the path to the OpenAPI
 contract file (i.e., `my-api.yaml` or `my-api.json`). 

The `--service-name`  is an optional parameter, which allows you to change the generated service name.

You can give the specific tags and operations that you need to document as services without documenting all the operations using these optional `--tags` and `--operations` commands.

`(-o|--output)` is an optional parameter. You can use this to give the output path for the generated files.
If not, it will take the execution path as the output path.

###### Modes
If you  want to generate a Service only, you can set the mode as `service` in the OpenAPI tool.

```bash
ballerina openapi   -i <openapi-contract> --mode service
                               [(-o|--output) output file path]
```

If you want to generate a Client only, you can set the mode as  `client` in the OpenAPI tool. 
This client can be used in client applications to call the service defined in the OpenAPI file.

```bash
ballerina openapi   -i <openapi-contract> --mode client
                               [(-o|--output) output file path]
```

#### Ballerina to OpenAPI
##### Service to OpenAPI Export
```bash
ballerina openapi   -i <ballerina file> 
                    [(-o|--output) output openapi file path]
```
Export the Ballerina service to an  OpenAPI Specification 3.0 definition. For the export to work properly, 
the input Ballerina service should be defined using the basic service and resource-level HTTP annotations.
If you need to document an OpenAPI contract for only one given service, then use this command.
```bash
    ballerina openapi -i <ballerina file> (-s | --service) <service name>
```

### OpenAPI Validator Compiler Plugin

The OpenAPI Validator Compiler plugin validates a service again a given OpenAPI contract. 
The Compiler Plugin is activated if a service has the `openapi:ServiceInfo` annotation. This plugin compares 
the service and the OpenAPI Contract and validates both against a pre-defined set of validation rules. 
If any of the rules fail, the plugin will give the result as one or more compilation errors.

#### Annotation for validator Plugin 
The `@openapi:ServiceInfo` annotation is used to bind the service with an OpenAPI Contract. You need to add 
this annotation to the service file with the required values for enabling the validations.  
The following is an example of the annotation usage.
```ballerina
@openapi:ServiceInfo{
    contract: “/path/to/openapi.json|yaml”,
    [ tag : “store” ],
    [ operations: [“op1”, “op2”] ] 
    [ failOnErrors]: true/false → default : true
    [ excludeTags ]: [“pets”, “user”]
    [ excludeOperations: [“op1”, “op2”] ]
   }
service greet on new http:Listener(9090) {
    ...
}
```
## Building from the Source

### Setting Up the Prerequisites

1. Download and install Java SE Development Kit (JDK) version 11 (from one of the following locations).

   * [Oracle](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
   
   * [OpenJDK](https://adoptopenjdk.net/)
   
        > **Note:** Set the JAVA_HOME environment variable to the path name of the directory into which you installed JDK.
     
### Building the Source

Execute the commands below to build from source.

1. To build the library:
        
        ./gradlew clean build

2. To run the integration tests:

        ./gradlew clean test

3. To build the module without the tests:

        ./gradlew clean build -x test

4. To publish to maven local:

        ./gradlew clean build publishToMavenLocal

## Contributing to Ballerina

As an open source project, Ballerina welcomes contributions from the community. 

You can also check for [open issues](https://github.com/ballerina-platform/module-ballerina-http/issues) that interest you. We look forward to receiving your contributions.

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of Conduct

All contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful Links

* Discuss about code changes of the Ballerina project in [ballerina-dev@googlegroups.com](mailto:ballerina-dev@googlegroups.com).
* Chat live with us via our [Slack channel](https://ballerina.io/community/slack/).
* Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
* View the [Ballerina performance test results](performance/benchmarks/summary.md).

