# Ballerina OpenAPI CLI Tool

The OpenAPI contract is a specification that creates a RESTful contract for APIs by detailing all of its resources and operations in a human and machine-readable format for easy development, discovery, and integration. Ballerina Swan Lake supports the OpenAPI contract version 3.0.0 onwards.

Ballerina OpenAPI tool makes it easy for you to start the development of a service documented in an OpenAPI contract in Ballerina by generating a Ballerina service and client skeletons. It enables you to take the code-first API design approach by generating an OpenAPI contract for the given service implementation.

## Features

The Ballerina OpenAPI tool provides the following capabilities:

1. **Generate Ballerina service/client stubs** from a given OpenAPI contract file using the CLI tool
2. **Export the OpenAPI definition** from a given Ballerina service implementation using the CLI tool
3. **Validate the service implementation compliance** with a provided OpenAPI contract using the OpenAPI annotation

> **Info:** The OpenAPI compiler plugin allows you to validate a service implementation against an OpenAPI contract during compile time. This plugin ensures that the implementation of a service does not deviate from its OpenAPI contract.

## Usage

### OpenAPI to Ballerina

Generate Ballerina service and client stubs from an OpenAPI contract:

```bash
bal openapi [-i | --input] <openapi-contract-file-path>
            [-o | --output] <output-location>
            [--mode] <mode-type>
            [--tags] <tag-names>
            [--operations] <operation-names>
            [-n | --nullable]
            [--license] <license-file-path>
            [--with-tests]
            [--status-code-binding] [--mock] [--with-service-contract]
            [--single-file] [--use-sanitized-oas]
```

### Ballerina to OpenAPI

Export OpenAPI definition from a Ballerina service:

```bash
bal openapi [-i | --input] <ballerina-service-file-path> [--json]
            [-s | --service] <current-service-name>
            [-o | --output] <output-location>
```

### Align OpenAPI Contract

Align OpenAPI contract according to Ballerina naming conventions:

```bash
bal openapi align [-i | --input] <openapi-contract-file-path>
                  [-o | --output] <output-file-path>
                  [-n | --name] <generated-file-name>
                  [-f | --format] [json|yaml]
                  [-t | --tags] <tag-names>
                  [--operations] <operation-names>
```

### Flatten OpenAPI Contract

Make OpenAPI contract more readable by relocating inline schemas:

```bash
bal openapi flatten [-i | --input] <openapi-contract-file-path>
                    [-o | --output] <output-file-path>
                    [-n | --name] <generated-file-name>
                    [-f | --format] [json|yaml]
                    [-t | --tags] <tag-names>
                    [--operations] <operation-names>
```

## Command Options

### OpenAPI to Ballerina Options

| Option | Description | Required |
|--------|-------------|----------|
| `-i \| --input` | Path of the OpenAPI contract file (e.g., my-api.yaml or my-api.json) | Yes |
| `-o \| --output` | Output directory location (defaults to current directory) | No |
| `--mode` | Mode type: service or client (defaults to both) | No |
| `--tags` | Generate stubs with specific tags only | No |
| `--operations` | Generate stubs with specific operations only | No |
| `-n \| --nullable` | Generate all data types with Ballerina nil support | No |
| `--license` | Add copyright/license header from file | No |
| `--with-tests` | Generate boilerplate tests for client methods | No |
| `--status-code-binding` | Generate status code binding | No |
| `--mock` | Generate mock service | No |
| `--with-service-contract` | Generate service contract | No |
| `--single-file` | Generate single file output | No |
| `--use-sanitized-oas` | Use sanitized OpenAPI specification | No |

### Ballerina to OpenAPI Options

| Option | Description | Required |
|--------|-------------|----------|
| `-i \| --input` | Path of the Ballerina service file | Yes |
| `--json` | Generate OpenAPI in JSON format (defaults to YAML) | No |
| `-s \| --service` | Name of the current service | No |
| `-o \| --output` | Output directory location | No |

### Align Options

| Option | Description | Required |
|--------|-------------|----------|
| `-i \| --input` | Path of the OpenAPI contract file | Yes |
| `-o \| --output` | Output directory location | No |
| `-n \| --name` | Generated file name (defaults to aligned_ballerina_openapi) | No |
| `-f \| --format` | Output format: json or yaml | No |
| `-t \| --tags` | Filter by specific tags | No |
| `--operations` | Filter by specific operations | No |

### Flatten Options

| Option | Description | Required |
|--------|-------------|----------|
| `-i \| --input` | Path of the OpenAPI contract file | Yes |
| `-o \| --output` | Output directory location | No |
| `-n \| --name` | Generated file name (defaults to flattened_openapi) | No |
| `-f \| --format` | Output format: json or yaml | No |
| `-t \| --tags` | Filter by specific tags | No |
| `--operations` | Filter by specific operations | No |

## OpenAPI Annotation

Use the `@openapi:ServiceInfo` annotation to configure OpenAPI generation:

```ballerina
@openapi:ServiceInfo {
    title: "Pet Store API",
    version: "1.0.0",
    description: "A sample Pet Store API",
    contactName: "API Support",
    contactEmail: "support@example.com",
    contactURL: "https://example.com/support",
    termsOfService: "https://example.com/terms",
    licenseName: "MIT",
    licenseURL: "https://opensource.org/licenses/MIT",
    embed: true
}
service /petstore on new http:Listener(9090) {
    // Service implementation
}
```

### Annotation Attributes

| Attribute | Description | Required |
|-----------|-------------|----------|
| `title: string` | API title | Yes |
| `version: string` | API version | No |
| `description: string` | API description | No |
| `email: string` | Contact email | No |
| `contactName: string` | Contact person/organization | No |
| `contactURL: string` | Contact webpage URL | No |
| `termsOfService: string` | Terms of service URL | No |
| `licenseName: string` | License name | No |
| `licenseURL: string` | License URL | No |
| `embed: boolean` | Enable introspection endpoint support | No |

## Examples

### Generate Service from OpenAPI

```bash
# Generate both service and client
bal openapi -i petstore.yaml

# Generate only service
bal openapi -i petstore.yaml --mode service

# Generate with specific tags
bal openapi -i petstore.yaml --tags "pets,users"

# Generate with nullable support
bal openapi -i petstore.yaml -n
```

### Export OpenAPI from Ballerina

```bash
# Export to YAML (default)
bal openapi -i service.bal

# Export to JSON
bal openapi -i service.bal --json

# Export specific service
bal openapi -i service.bal -s PetStoreService
```

### Align OpenAPI Contract

```bash
# Align to Ballerina naming conventions
bal openapi align -i api.yaml

# Align with custom output
bal openapi align -i api.yaml -o ./aligned -n my_api.yaml
```

### Flatten OpenAPI Contract

```bash
# Flatten inline schemas
bal openapi flatten -i api.yaml

# Flatten with custom output
bal openapi flatten -i api.yaml -o ./flattened -f json
```

## OpenAPI Contract Modifier

### Ballerina-Preferred Naming Conventions

The `align` subcommand adds Ballerina-specific name extensions (`x-ballerina-name`) to schemas and properties that cannot be modified directly, ensuring compatibility with Ballerina naming conventions.

### Inline Schema Extraction

The `flatten` subcommand relocates all inline embedded schemas to the components section, making the OpenAPI contract more readable and maintainable.

## Learn More

- [Ballerina OpenAPI Tool Documentation](https://ballerina.io/learn/openapi-tool/)
- [Ballerina Language Documentation](https://ballerina.io/learn/)
- [OpenAPI Specification](https://spec.openapis.org/)
- [Ballerina by Example](https://ballerina.io/learn/by-example/)

## Contributing

This project is part of the Ballerina OpenAPI tools ecosystem. For contributing guidelines and development setup, please refer to the main project repository.

## License

This project is licensed under the Apache License 2.0.
