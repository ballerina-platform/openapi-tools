## Overview

This module provides comprehensive annotations and utilities to enhance the seamless interoperability between Ballerina and OpenAPI specifications.

The OpenAPI module delivers the following key capabilities:

1. **Contract Enhancement**: Annotations to enrich and customize OpenAPI contracts generated from Ballerina services
2. **Contract Validation**: Annotations to facilitate compile-time validation of Ballerina services against existing OpenAPI contracts
3. **Runtime Introspection**: Built-in support for exposing OpenAPI documentation as introspection resources

## Annotations for Enriching Generated OpenAPI Contracts

### ServiceInfo Annotation

The `@openapi:ServiceInfo` annotation configures service-level metadata for OpenAPI contract generation, including title, description, contact information, and version details.

```ballerina
@openapi:ServiceInfo {
    contract: "/path/to/openapi.json|yaml",
    title: "Store Management API",
    version: "1.0.0",
    description: "Comprehensive API for managing retail store operations"
}
service /store on new http:Listener(8080) {
    // Service implementation
}
```

#### Configuration Fields

| Field Name     | Type    | Description                                                                                                                                                                                      |
|----------------|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| contract       | string  | Path to an existing OpenAPI contract file (.yaml or .json). When specified, the tool uses this contract as the base. If omitted, generates a new contract from the Ballerina service definition. |
| title          | string  | Sets the API title in the info section. Defaults to the service's absolute base path if not provided.                                                                                            |
| version        | string  | Specifies the API version in the info section. Defaults to the Ballerina package version if not specified.                                                                                       |
| description    | string  | Provides a detailed description of the API's purpose, features, and usage guidelines for the info section.                                                                                       |
| email          | string  | Contact email address for API support or inquiries.                                                                                                                                              |
| contactName    | string  | Name of the person or organization responsible for the API.                                                                                                                                      |
| contactURL     | string  | URL to a webpage with additional API information, documentation, or support resources.                                                                                                           |
| termsOfService | string  | URL pointing to the terms of service governing API usage.                                                                                                                                        |
| licenseName    | string  | Name of the license under which the API is distributed.                                                                                                                                          |
| licenseURL     | string  | URL to the complete license text and terms.                                                                                                                                                      |
| embed          | boolean | When set to `true`, exposes the OpenAPI contract as an introspection resource at runtime.                                                                                                        |
| failOnErrors   | boolean | Controls validation behavior. When `true` (default), contract violations generate compilation errors. When `false`, violations are reported as warnings.                                         |

> **Note:** All fields in the `@openapi:ServiceInfo` annotation are optional.

### ResourceInfo Annotation

The `@openapi:ResourceInfo` annotation enriches individual resource functions with operation-specific metadata such as operation IDs, summaries, tags, and examples.

```ballerina
@openapi:ResourceInfo {
    operationId: "createStoreInventory",
    summary: "Add new inventory items to store",
    tags: ["inventory", "retail", "management"],
    examples: {
        requestBody: {
            "application/json": {
                value: {
                    itemId: "ITEM001",
                    quantity: 50,
                    price: 29.99
                }
            }
        }
    }
}
resource function post inventory(Inventory payload) returns InventoryResponse|error {
    // Resource implementation
}
```

#### Configuration Fields

| Field Name  | Type     | Description                                                                                                           |
|-------------|----------|-----------------------------------------------------------------------------------------------------------------------|
| operationId | string   | Unique identifier for the operation within the OpenAPI contract. Useful for code generation and API client libraries. |
| summary     | string   | Brief, descriptive summary of the operation's purpose and functionality.                                              |
| tags        | string[] | Array of tags for logical grouping and organization of operations in documentation.                                   |
| examples    | Examples | Comprehensive examples for the operation, including request bodies, responses, and parameter values.                  |

> **Note:** All fields in the `@openapi:ResourceInfo` annotation are optional.

### Example Annotation

The `@openapi:Example` annotation provides concrete examples for types, record fields, and parameters to improve API documentation and developer experience.

```ballerina
// Type-level example
@openapi:Example {
    value: {
        id: 12345,
        name: "Jessica Smith",
        email: "jessica.smith@example.com",
        role: "admin"
    }
}
type User record {
    int id;
    string name;
    string email;
    string role;
};

// Field-level examples
type Product record {
    @openapi:Example { value: "PROD-001" }
    string productId;
    
    @openapi:Example { value: "Premium Wireless Headphones" }
    string name;
    
    @openapi:Example { value: 199.99 }
    decimal price;
};

// Parameter example
resource function get orders(
    @openapi:Example { value: "approved" } 
    "approved"|"pending"|"cancelled"|"shipped" status,
    
    @openapi:Example { value: 10 }
    int? 'limit = 20
) returns Order[]|error {
    // Implementation
}
```

## Runtime OpenAPI Contract Introspection

Enable runtime exposure of OpenAPI contracts by setting the `embed` field to `true` in the `@openapi:ServiceInfo` annotation. This creates introspection endpoints that serve the OpenAPI specification and interactive documentation.

```ballerina
@openapi:ServiceInfo {
    embed: true,
    title: "Hello World API",
    description: "A simple greeting service with embedded OpenAPI documentation"
}
service /hello on new http:Listener(9090) {
    resource function get greeting(string? name = "World") returns string {
        return string `Hello, ${name}!`;
    }
}
```

### Accessing Introspection Resources

An OPTIONS request to the service reveals available introspection endpoints:

```bash
curl -v localhost:9090/hello -X OPTIONS
```

**Response:**

```http
HTTP/1.1 204 No Content
allow: GET, OPTIONS
link: </hello/openapi-doc-dygixywsw>;rel="service-desc", 
      </hello/swagger-ui-dygixywsw>;rel="swagger-ui"
server: ballerina
date: Thu, 13 Jun 2024 20:04:11 +0530
```

The response includes links to:

- **OpenAPI Specification** (`rel="service-desc"`): Raw OpenAPI JSON/YAML document
- **Swagger UI** (`rel="swagger-ui"`): Interactive API documentation interface

## Contract-First Development with Validation

Use the `@openapi:ServiceInfo` annotation to validate Ballerina services against existing OpenAPI contracts at compile time, ensuring API compliance and consistency.

```ballerina
@openapi:ServiceInfo {
    contract: "petstore-api.yaml",
    failOnErrors: true  // Strict validation (default)
}
service /petstore on new http:Listener(8080) {
    resource function get pets() returns Pet[]|error {
        // Implementation must match contract specification
    }
    
    resource function post pets(NewPet payload) returns Pet|error {
        // Implementation validated against contract
    }
}
```

### Validation Modes

- **Strict Mode** (`failOnErrors: true`): Contract violations generate compilation errors, preventing deployment of non-compliant services
- **Warning Mode** (`failOnErrors: false`): Contract violations are reported as warnings, allowing deployment with notifications

> **Best Practice**: For contract-first development, consider generating service contract types from the OpenAPI specification and implementing services against these types. This approach provides stronger type safety and compile-time guarantees. See [Generate service contract from OpenAPI](https://ballerina.io/learn/openapi-tool/#generate-service-contract-object-for-given-openapi-contract) for detailed guidance.
