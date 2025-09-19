## Overview

The OpenAPI contract is a specification that creates a RESTful contract for APIs by detailing all of its resources and operations in a human and machine-readable format for easy development, discovery, and integration. The Ballerina OpenAPI tool makes it easy for you to start the development of a service documented in an OpenAPI contract in Ballerina by generating a Ballerina service and client skeletons facilitating contract-first development. It also enables you to take the code-first API design approach by generating an OpenAPI contract for the given service implementation.

The Ballerina OpenAPI tool provides the following capabilities:

1. **Modify OpenAPI contracts** to be more compatible with Ballerina by aligning names to Ballerina naming conventions and flattening inline schemas
2. **Generate Ballerina service/client stubs** from a given OpenAPI contract file
3. **Export the OpenAPI contract** from a given Ballerina service implementation
4. **Integrate client generation from the OpenAPI contract** as a build option in Ballerina projects

## Commands

### `bal openapi flatten`

Make the OpenAPI contract more readable by relocating all inline embedded schemas to the components section and assigning each a unique, Ballerina-friendly name.

**Synopsis:**

```bash
bal openapi flatten -i | --input <openapi-contract-file-path>
                    [-o | --output <output-file-path>]
                    [-n | --name <generated-file-name>]
                    [-f | --format <json|yaml>]
                    [-t | --tags <tag-names>]
                    [--operations <operation-names>]
```

**Options:**

| Option           | Description                                               | Required |
|------------------|-----------------------------------------------------------|----------|
| `-i`, `--input`  | Path of the OpenAPI contract file                         | Yes      |
| `-o`, `--output` | Output directory location (defaults to current directory) | No       |
| `-n`, `--name`   | Generated file name (defaults to `flattened_openapi`)     | No       |
| `-f`, `--format` | Output format: `json` or `yaml`                           | No       |
| `-t`, `--tags`   | Filter by specific tags                                   | No       |
| `--operations`   | Filter by specific operations                             | No       |

### `bal openapi align`

Align the OpenAPI contract file according to the best practices of Ballerina.

**Default alignments:**

- **`name`**: Align according to the best naming practices of Ballerina. The Ballerina name extensions are added to the schemas which cannot be modified directly.
- **`doc`**: Align according to the best documentation practices of Ballerina.
- **`basepath`**: Extract the common base path from the operations and add it to the server URL.

The alignments can be filtered using the `--include` and `--exclude` options. The `--include` option has higher priority than the `--exclude` option.

**Synopsis:**

```bash
bal openapi align -i | --input <openapi-contract-file-path>
                  [-o | --output <output-file-path>]
                  [-n | --name <generated-file-name>]
                  [-f | --format <json|yaml>]
                  [-t | --tags <tag-names>]
                  [--operations <operation-names>]
                  [--include <alignment-names>]
                  [--exclude <alignment-names>]
```

**Options:**

| Option           | Description                                                                    | Required |
|------------------|--------------------------------------------------------------------------------|----------|
| `-i`, `--input`  | Path of the OpenAPI contract file                                              | Yes      |
| `-o`, `--output` | Output directory location (defaults to current directory)                      | No       |
| `-n`, `--name`   | Generated file name (defaults to `aligned_ballerina_openapi`)                  | No       |
| `-f`, `--format` | Output format: `json` or `yaml`                                                | No       |
| `-t`, `--tags`   | Filter by specific tags                                                        | No       |
| `--operations`   | Filter by specific operations                                                  | No       |
| `--include`      | Comma-separated list of alignment names to include (`name`, `doc`, `basepath`) | No       |
| `--exclude`      | Comma-separated list of alignment names to exclude (`name`, `doc`, `basepath`) | No       |

### `bal openapi` (OpenAPI to Ballerina)

Generate Ballerina service and client stubs from an OpenAPI contract.

**Synopsis:**

```bash
bal openapi -i | --input <openapi-contract-file-path>
            [-o | --output <output-directory>]
            [--mode <mode-type>]
            [--tags <tag-names>]
            [--operations <operation-names>]
            [-n | --nullable]
            [--license <license-file-path>]
            [--with-tests]
            [--client-methods <resource|remote>]
            [--without-data-binding]
            [--status-code-binding]
            [--mock]
            [--with-service-contract]
            [--single-file]
```

**Options:**

| Option                    | Description                                                                                | Required |
|---------------------------|--------------------------------------------------------------------------------------------|----------|
| `-i`, `--input`           | Path of the OpenAPI contract file                                                          | Yes      |
| `-o`, `--output`          | Output directory location (defaults to current directory)                                  | No       |
| `--mode`                  | Generation mode: `service`, `client`, or both (default: both)                              | No       |
| `--tags`                  | Filter operations by specific tags for service/client generation                           | No       |
| `--operations`            | List of specific operations to generate                                                    | No       |
| `-n`, `--nullable`        | Generate all data types with Ballerina nil support for safer JSON schema to record binding | No       |
| `--license`               | Add copyright/license header from specified file path                                      | No       |
| `--with-tests`            | Generate test boilerplate for all remote functions in client generation                    | No       |
| `--client-methods`        | Client method type: `resource` (default) or `remote`                                       | No       |
| `--without-data-binding`  | Generate low-level service without data-binding logic                                      | No       |
| `--status-code-binding`   | Generate client methods with status code response binding                                  | No       |
| `--mock`                  | Generate mock client for the given OpenAPI contract                                        | No       |
| `--with-service-contract` | Generate service contract type for the OpenAPI contract                                    | No       |
| `--single-file`           | Generate service or client with related types and utilities in a single file               | No       |

### `bal openapi` (Ballerina to OpenAPI)

Export OpenAPI definition from a Ballerina service.

**Synopsis:**

```bash
bal openapi -i | --input <ballerina-service-file-path>
            [-o | --output <output-location>]
            [-s | --service <service-name>]
            [--json]
```

**Options:**

| Option            | Description                                               | Required |
|-------------------|-----------------------------------------------------------|----------|
| `-i`, `--input`   | Path of the Ballerina service file                        | Yes      |
| `-o`, `--output`  | Output directory location (defaults to current directory) | No       |
| `-s`, `--service` | Name of the current service                               | No       |
| `--json`          | Generate OpenAPI output in JSON format (defaults to YAML) | No       |

### `bal openapi add`

Update the `Ballerina.toml` file with OpenAPI tool configuration details for generating a Ballerina service or client as part of the build process.

**Synopsis:**

```bash
bal openapi add -i | --input <openapi-contract-file-path>
                [--id <service/client-id>]
                [-p | --package <package-location>]
                [--module <module-name>]
                [--mode <mode-type>]
                [--tags <tag-names>]
                [--operations <operation-names>]
                [-n | --nullable]
                [--license <license-file-path>]
                [--client-methods <resource|remote>]
                [--status-code-binding]
```

**Options:**

| Option                  | Description                                                                 | Required |
|-------------------------|-----------------------------------------------------------------------------|----------|
| `-i`, `--input`         | Path of the OpenAPI contract file                                           | Yes      |
| `--id`                  | Unique identifier for the service/client in `Ballerina.toml`                | No       |
| `-p`, `--package`       | Package location to update `Ballerina.toml` (defaults to current directory) | No       |
| `--module`              | Module name for generated service/client                                    | No       |
| `--mode`                | Generation mode: `service` or `client` (default: `client`)                  | No       |
| `--tags`                | Filter operations by specific tags                                          | No       |
| `--operations`          | List of specific operations to generate                                     | No       |
| `-n`, `--nullable`      | Generate all data types with Ballerina nil support                          | No       |
| `--license`             | Add copyright/license header from specified file path                       | No       |
| `--client-methods`      | Client method type: `resource` (default) or `remote`                        | No       |
| `--status-code-binding` | Generate client methods with status code response binding                   | No       |

## Examples

### Flatten OpenAPI contracts

```bash
# Basic flattening
bal openapi flatten -i api.yaml

# Flatten with custom output directory and JSON format
bal openapi flatten -i api.yaml -o ./flattened -f json

# Flatten specific tags only
bal openapi flatten -i api.yaml -t "pets,users" -n pets_flattened
```

### Align OpenAPI contracts

```bash
# Basic alignment with all default alignments
bal openapi align -i api.yaml

# Align with custom output and specific alignments
bal openapi align -i api.yaml -o ./aligned -n my_api.yaml --include "name,doc"

# Align excluding basepath alignment
bal openapi align -i api.yaml --exclude "basepath"
```

### Generate Ballerina code from OpenAPI

```bash
# Generate both service and client
bal openapi -i petstore.yaml

# Generate only service with nullable support
bal openapi -i petstore.yaml --mode service -n

# Generate client with specific tags and test files
bal openapi -i petstore.yaml --mode client --tags "pets,users" --with-tests

# Generate with remote client methods and status code binding
bal openapi -i petstore.yaml --client-methods remote --status-code-binding

# Generate mock client for testing
bal openapi -i petstore.yaml --mode client --mock

# Generate in a single file with service contract
bal openapi -i petstore.yaml --single-file --with-service-contract
```

### Export OpenAPI from Ballerina

```bash
# Export to YAML (default format)
bal openapi -i service.bal

# Export to JSON with specific output directory
bal openapi -i service.bal --json -o ./openapi-specs

# Export specific service by name
bal openapi -i service.bal -s PetStoreService -o ./specs
```

### Add OpenAPI client generation as a build option

```bash
# Add client generation to Ballerina.toml
bal openapi add -i petstore.yaml --id petstoreClient --module petstore
```

### Advanced usage scenarios

```bash
# Contract-first development
bal openapi flatten -i original-api.yaml
bal openapi align -i flattened_openapi.yaml --include "name,doc"
bal openapi -i aligned_ballerina_openapi.yaml --mode service --with-service-contract

# Code-first development
bal openapi -i my_service.bal --json -o ./api-docs
bal openapi -i ./api-docs/openapi.json --mode client --mock
```
