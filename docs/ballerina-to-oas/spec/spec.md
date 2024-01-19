# OpenAPI tool specification: Ballerina to OpenAPI

Owners: @shafreenAnfar @TharmiganK @lnash94                 
Reviewers: @shafreenAnfar @TharmiganK @lnash94 @dilanSachi           
Created: 2024/01/10          
Updated: 2024/01/10        
Edition: Swan Lake

The Ballerina to OpenAPI tool can generate an OpenAPI specification for a Ballerina service. This specification describes the Ballerina service to OpenAPI mapping.

## Table of contents

- [OpenAPI tool specification: Ballerina to OpenAPI](#openapi-tool-specification-ballerina-to-openapi)
    - [Table of contents](#table-of-contents)
    - [Section 1: The `info` section](#section-1-the-info-section)
        - [Title of the API](#title-of-the-api)
        - [Version of the API](#version-of-the-api)
        - [The `ServiceInfo` annotation](#the-serviceinfo-annotation)
    - [Section 2: The `servers` section](#section-2-the-servers-section)
    - [Section 3: The `paths` section](#section-3-the-paths-section)
        - [Path](#path)
        - [Operation](#operation)
            - [The `operationId` field](#the-operationid-field)
            - [The `responses` field](#the-responses-field)
                - [The status code](#the-status-code)
                - [The media type](#the-media-type)
                - [The return type schema](#the-return-type-schema)
                - [The headers](#the-headers)
                    - [The cache headers](#the-cache-headers)
                    - [The headers from status code response](#the-headers-from-the-status-code-response)
            - [The `requestBody` field](#the-requestbody-field)
                - [The request body accessed using the `http:Request` object](#the-httprequest-object)
                - [The request body accessed using data binding](#the-payload-data-binding)
            - [The `parameters` field](#the-parameters-field)
                - [The path parameter](#the-path-parameter)
                - [The query parameter](#the-query-parameter)
                - [The header parameter](#the-header-parameter)
    - [Section 4: The `components` section](#section-4-the-components-section)
        - [The Ballerina type to schema mapping](#the-ballerina-type-to-schema-mapping)
            - [Ballerina basic types](#ballerina-basic-types)
            - [Ballerina structural types](#ballerina-structural-types)
        - [Ballerina constraints mapping to type schema](#ballerina-constraints-mapping-to-type-schema)
            - [Integer constraints](#integer-constraints)
            - [Float constraints](#float-constraints)
            - [Number constraints](#number-constraints)
            - [String constraints](#string-constraints)
            - [Array constraints](#array-constraints)
            - [Date constraints](#date-constraints)

## Section 1: The `info` section

The [`info` section](https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#infoObject) provides metadata about the API.

The Ballerina OpenAPI tool populates the following fields of the `info` section:

- `title`: The title of the API
- `version`: The version of the API

```yml
info:
title: Title of the API
version: 1.0.0
```

### Title of the API

The title of the API is derived from the Ballerina service base path.

| Base path    | Title of the API                            |
|--------------|---------------------------------------------|
| empty or `/` | The name of the file containing the service |
| `/api`       | `Api`                                       |
| `/api/v1`    | `Api V1`                                    |

### Version of the API

The version of the API is derived from the Ballerina package version. If the file is not in a package, the version is set to `1.0.0`.

### The `ServiceInfo` annotation

The `ServiceInfo` annotation can be used to override the title and version of the API.

```ballerina
@openapi:ServiceInfo {
  title: "My API",
  'version: "2.0.0"
}
service /api/v1 on new http:Listener(9090) {
  // ...
}
```

```yml
info:
title: My API
version: 2.0.0
```

## Section 2: The `servers` section

The `servers` section specifies the API servers and corresponding base URLs. This section has a list of [`server`](https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#server-object) objects.

The Ballerina OpenAPI tool populates the `url` field of the `server` objects in this section. The `url` field is derived from the Ballerina service object and the listener configuration. The `url` field is constructed using the following information:

| Field       | Description                                                                                                                             |
|-------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| `scheme`    | The `scheme` field derived from the listener definition                                                                                 |
| `server`    | The `host` field of the listener configuration. If the `host` field is not specified, defaults to `localhost`                           |
| `port`      | The `port` field of the listener configuration. If the `port` field is not specified, defaults to `80` for `http` and `443` for `https` |
| `base-path` | The base path retrieved from the Ballerina service object                                                                               |

```yml
servers:
- url: "<scheme>://{server}:{port}/<base-path>"
  variables:
    server:
      default: <sever-name>
    port:
      default: <port>
```

A Ballerina service can be attached to multiple listeners.

```ballerina
listener http:Listener listenerEp1 = new (9090);
listener http:Listener listenerEp2 = new (9091, config = {host: "mocksvc.io"});

service /basepath on listenerEp1, listenerEp2 {

  // ...
}
```

For the above service, the following `servers` section is generated.

```yml
servers:
- url: "{server}:{port}/basepath"
variables:
  server:
    enum:
    - mocksvc.io
    - http://localhost
    default: http://localhost
  port:
    enum:
    - "9091"
    - "9090"
    default: "9090"
```

## Section 3: The `paths` section

The `paths` section defines the available API endpoints(paths) and the HTTP methods supported by them(operations).

### Path

A `path` object is defined by a unique path and may contain one or more operations.

The `path` name is derived from the path of the Ballerina resource function.

```ballerina
service /basepath on new http:Listener(9090) {

  resource function get path {
      // ...
  }
}
```

```yml
paths:
/path:
  # ...
```

If the resource has a dot resource path, then the path name is defined as a slash.

```ballerina
service /basepath on new http:Listener(9090) {

  resource function get . {
      // ...
  }
}
```

```yml
paths:
/:
  # ...
```

When the resource function has a path parameter, the path name is derived from the path of the Ballerina resource function with the path parameter in curly braces.

```ballerina
service /basepath on new http:Listener(9090) {

  resource function get path/[string param] {
      // ...
  }
}
```

```yml
paths:
/path/{param}:
  # ...
```

### Operation

For each `path` object, operations define the accessible HTTP methods.

The Ballerina OpenAPI tool supports the following HTTP methods: `get`, `post`, `put`, `patch`, `delete`, `head`, `options`, and `trace`.

The Ballerina default resource is not supported.

```ballerina
service /payloadV on new http:Listener(9090) {

   resource function default path () {
       // ...
   }
}
```

```console
WARNING Generated OpenAPI definition does not contain details for the `default` resource method in the Ballerina service.
```

An operation is defined by the following fields:

- `operationId`
- `responses`
- `requestBody`
- `parameters`

#### The `operationId` field

The operation object will contain a unique operation ID. This operation ID is derived from the Ballerina resource function method and path.

Example:
| Ballerina resource function                  | Operation ID   |
| -------------------------------------------- | -------------- |
| `resource function get path`                 | getPath        |
| `resource function post path1/path2`         | postPath1Path2 |
| `resource function post path/[string param]` | postPathParam  |

#### The `responses` field

The `responses` field contains the expected responses for the Ballerina resource function described by the operation object. This is a required field.

This follows the [OpenAPI specification](https://swagger.io/docs/specification/describing-responses/).

```yml
responses:
  <status-code>:
     description: <description-from-status-code>
     content:
        <media-type>:
           schema: <return-type-schema>
           headers:
              <header-name>: <header-type-schema>


   # Definition of all error statuses
  default:
```

The Ballerina HTTP resource can return one of the following types:

1. `anydata`
2. `http:StatusCodeResponse`
3. `http:Response`
4. `error`

##### The status code

The status code is inferred from the Ballerina resource function return type and the description is populated according to the status code.

If the return type is an `http:StatusCodeResponse`, then the status code is derived from the type of the `http:StatusCodeResponse`. Otherwise, the default status code will be used depending on the HTTP method and return type.

| HTTP Method | Default status code for success response |
|-------------|------------------------------------------|
| `post`      | `201`                                    |
| other       | `200`                                    |

| Return Type                  | Status code                                       |
|------------------------------|---------------------------------------------------|
| `nil`                        | `202`                                             |
| `error`                      | `500`                                             |
| `anydata` (other than `nil`) | default success status code using the HTTP method |
| `http:StatusCodeResponse`    | derived from the type of the `StatusCodeResponse` |
| `http:Response`              | `default`                                         |

##### The media type

The media type is inferred from the effective type of the Ballerina resource function return type.

| Effective Type | Inferred media type        |
|----------------|----------------------------|
| `byte[]`       | `application/octet-stream` |
| `string`       | `text/plain`               |
| `xml`          | `application/xml`          |
| `Other`        | `application/json`         |

This default media type can be overridden using the `@http:Payload` annotation in the return type.

```ballerina
service /api on new http:Listener(9090) {

   resource function get users/[string userId]/uploads/[string docId]() returns @http:Payload {mediaType: "text/csv"} string {
     // ...
   }
}
```

Additionally, if the service defines a media type subtype prefix via `@http:ServiceConfig` annotation then the media type is prefixed with the subtype.

```ballerina
@http:ServiceConfig {
 mediaTypeSubtypePrefix: "vnd.example"
}
service /api on new http:Listener(9090) {

   resource function get users/[string userId]() returns User {
     // ...
   }
}
``````

##### The return type schema

The return type schema is derived as follows for each of these return types:

<table>

<tr>
<th>Response Type</th>
<th>Mapping</th>
</tr>

<tr>
<td><code>anydata</code> (other than <code>nil</code>)</td>
<td>

```yml
responses:
 <default-status-code>:
   description:
   content:
     <media-type>:
       schema: <type-schema>
       headers:
         <header-name>: <type-schema>
```

</td>
</tr>

<tr>
<td><code>nil</code></td>
<td>

```yml
responses:
 "202":
   description: "Accepted"
```

</td>
</tr>

<tr>
<td><code>http:Response</code></td>
<td>

```yml
responses:
 default:
   description: "Any Response"
   content:
     '*/*':
        schema:
          description: "Any type of entity body"
        headers:
          <header-name>: <type-schema>
```

</td>
</tr>

<tr>
<td><code>http:StatusCodeResponse</code></td>
<td>

```yml
responses:
 <status-code>:
   description:
     content:
       <media-type>:
         schema: <type-schema>
         headers:
           <header-name>: <type-schema>
```

</td>
</tr>
</table>

> **Note:**
>
> - Here the types `anydata` and `http:StatusCodeResponse` can generate multiple response types if the body type contains a union type that has different effective media types.
> - If the return type is a union type, then the return type is split into multiple types which have a single effective media type. Each of these types will be considered as a separate response.
> - If the return type is a union of `anydata` and `http:StatusCodeResponse`, the union of `anydata` and the `body` of the `http:StatusCodeResponse` will be considered as the return type only if the `http:StatusCodeResponse` produces the same status code as the `anydata` type.

##### The headers

###### The cache headers

The HTTP cache headers are derived from the `@http:CacheConfig` annotation in the return type. The following HTTP cache headers are populated according to the configuration.

- `Cache-Control`
- `Last-Modified`
- `ETag`

The default value for `Cache-Control` is populated with the directives according to the cache configuration.

```ballerina
service / on new http:Listener(9090) {

   resource function get albums/[string title]() returns @http:Cache {maxAge: 15} Album {
       // ...
   }
}
```

```yml
headers:
  Last-Modified:
     schema:
        type: string
  Cache-Control:
     schema:
        type: string
        default: "must-revalidate,public,max-age=15"
  ETag:
     schema:
        type: string
```

**Note:** Since caching is only allowed for successful responses, the cache headers are only populated for successful responses.

###### The headers from the status code response

The `http:StatusCodeResponse` type can contain headers. If the headers are specified as a record type then the headers are derived from the fields of that record type.

```ballerina
type OrgHeaders record {|
   string xOrg = "ballerina";
   string[] xApiKeys;
   string xVersion?;
|};

type AlbumCreated record {|
 *http:Created;
 OrgHeaders headers;
 Album body;
|};

service / on new http:Listener(9090) {
   resource function get albums/[string title]() returns AlbumCreated {
       // ...
   }
}
```

```yml
headers:
  xOrg:
     schema:
        type: string
        default: ballerina
  xApiKeys:
     schema:
        type: array
        items:
           type: string
  xVersion:
     schema:
        type: string
```

#### The `requestBody` field

The `requestBody` field describes the request body content for the Ballerina resource function described by the operation object. This is an optional field and is only populated if the Ballerina resource function accesses the request payload.

```yml
requestBody:
  description: <description>
  required: <required>
  content:
     <media-type>:
        schema: <request-body-type-schema>
```

This field is skipped for the `GET` method.

```ballerina
service /api on new http:Listener(9090) {

   resource function get path(http:Request req) {
     // ...
   }
}
```

```console
WARNING Generated OpenAPI definition does not contain `http:Request` body information of the `GET` method, as it's not supported by the OpenAPI specification.
```

Ballerina resource functions can access the request body using the `http:Request` object or can be directly bound to an `anydata` type (using the `@http:Payload` annotation - The annotation is optional when the payload is bound to a structural type).

##### The `http:Request` object

```ballerina
service /api on new http:Listener(9090) {

   resource function post path(http:Request req) {
        // ...
   }
}
```

```yml
requestBody:
  content:
     '*/*':
     schema:
        description: Any type of entity body
```

##### The payload data binding

```ballerina
service /api on new http:Listener(9090) {

   resource function post path(User user) {
        // ...
   }
}
```

```yml
requestBody:
  required: true
  content:
     application/json:
        schema:
           $ref: "#/components/schemas/User"
```

Here the `media-type` is inferred from the payload bound type, and the logic is the same as the return type media type inference.

If the payload bound type includes a `nil` type, then the `required` field is set to `false`.

```ballerina
service /api on new http:Listener(9090) {

   resource function post path(User? user) {
        // ...
   }
}
```

```yml
requestBody:
  required: false
  content:
     application/json:
        schema:
           $ref: "#/components/schemas/User"
```

> **Note:** If the resource function accesses the payload using the `http:Request` object and also binds the payload to a type, then the `requestBody` field is populated with the bound type.

#### The `parameters` field

The `parameters` field describes the parameters that can be used in the Ballerina resource function described by the operation object. This is an optional field.

This field includes the following parameters:

- Path parameters
- Query parameters
- Header parameters

##### The path parameter

The sample schema for a path parameter is as follows:

```yml
parameters:
  - name: <name>
    in: path
    description: <description>
    required: true
    schema: <type-schema>
```

The path parameter name should be the same as the path parameter defined in the `path` object and path parameters are always required.

The path parameter is derived from the path parameter type defined in the Ballerina resource path. Ballerina supports the following path parameter types:

- `string`
- `int`
- `float`
- `decimal`
- `boolean`

The Ballerina OpenAPI tool does not support Ballerina resource functions with the rest parameter as a path parameter.

```ballerina
service /basepath on new http:Listener(9090) {

  resource function get path/[string... params] {
      // ...
  }
}
```

```console
WARNING Generated OpenAPI specification excludes details for operation with rest parameter in the resource `path`
```

##### The query parameter

The sample schema for a query parameter is as follows:

```yml
parameters:
  - name: <name>
    in: query
    description: <description>
    required: <required>
    schema: <type-schema>
```

The query parameter is derived from the query parameter type defined in the Ballerina resource function. Ballerina supports the following query parameter types:

- `string`
- `int`
- `float`
- `decimal`
- `boolean`
- `map<anydata>`
- array of the above types

Additionally, the query parameter type can be nilable.

The query parameter is not required in the following cases:

- The query parameter type is nilable and the `treatNilableAsOptional` option in service configuration is set to `true` (Default is `true`).

  ```ballerina
   service /api on new http:Listener(9090) {

      resource function get path(string? param) {
         // ...
      }
   }
   ```

  ```yml
  parameters:
     - name: param
       in: query
       schema:
         type: string
         nullable: true
  ```

- The query parameter has a default value

  ```ballerina
  service /api on new http:Listener(9090) {
 
      resource function get path(string param = "default") {
         // ...
      }
  }
  ```

  ```yml
  parameters:
     - name: param
       in: query
       schema:
         type: string
         default: default
  ```

If the `treatNilableAsOptional` option in the service configuration is set to `false`, then even if the query parameter type is nilable, the query parameter is required.

```ballerina
@http:ServiceConfig {
 treatNilableAsOptional: false
}
service /api on new http:Listener(9090) {

  resource function get path(string? param) {
      // ...
  }
}
```

```yml
parameters:
  - name: param
    in: query
    required: true
    schema:
      type: string
      nullable: true
```

If the query parameter type is a `map` or `record` with `anydata` fields, then the query parameter schema is wrapped with `content` and `application/json` to indicate that the query parameter should be a JSON object which should be encoded properly.

```ballerina
service /api on new http:Listener(9090) {

  resource function get path(@http:Query User param) {
      // ...
  }
}
```

```yml
parameters:
  - name: param
    in: query
    required: true
    content:
      application/json:
        schema:
          $ref: "#/components/schemas/User"
```

##### The header parameter

The sample schema for a header parameter is as follows:

```yml
parameters:
  - name: <name>
    in: header
    description: <description>
    required: <required>
    schema: <type-schema>
```

The header parameter is derived from the header parameter type defined in the Ballerina resource function using the `@http:Header` annotation. Ballerina supports the following header parameter types:

- `string`
- `int`
- `float`
- `decimal`
- `boolean`
- array of the above types

The header parameter type can be nilable.

The header parameter is not required in the following cases:

- The header parameter type is nilable and the `treatNilableAsOptional` option in service configuration is set to `true` (Default is `true`).

  ```ballerina
   service /api on new http:Listener(9090) {
      resource function get path(@http:Header string? param) {
         // ...
      }
   }
  ```

  ```yml
  parameters:
     - name: param
       in: header
       schema:
         type: string
         nullable: true
  ```

- The header parameter has a default value

  ```ballerina
  service /api on new http:Listener(9090) {
 
      resource function get path(@http:Header string param = "default") {
         // ...
      }
  }
  ```

  ```yml
  parameters:
     - name: param
       in: header
       schema:
         type: string
         default: default
  ```

If the `treatNilableAsOptional` option in the service configuration is set to `false`, then even if the header parameter type is nilable, the header parameter is required.

```ballerina
@http:ServiceConfig {
  treatNilableAsOptional: false
}
service /api on new http:Listener(9090) {

  resource function get path(@http:Header string? param) {
        // ...
  }
}
```

```yml
parameters:
  -  name: param
     in: header
     required: true
     schema:
        type: string
        nullable: true
```

Additionally, the header parameter can be a closed `record` which contains the above basic types as fields. In that case, each field of this record represents a header parameter.

```ballerina
type OrgHeaders record {|
   string xOrg = "ballerina";
   string[] xApiKeys;
   string xVersion?;
|};

service /api on new http:Listener(9090) {

  resource function get path(@http:Header OrgHeaders headers) {
        // ...
  }
}
```

```yml
parameters:
 - name: xApiKeys
   in: header
   required: true
   schema:
     type: array
     items:
       type: string
 - name: xVersion
   in: header
   schema:
     type: string
 - name: xOrg
   in: header
   schema:
     type: string
     default: ballerina
```

## Section 4: The `components` section

The `components` section defines reusable components that can be used across the API specification. This section has the type schemas generated for Ballerina types. The component name is the same as the Ballerina type name.

### The Ballerina type to schema mapping

#### Ballerina basic types

<table>
<tr>
<th>Basic Type</th>
<th>Mapping</th>
</tr>
<tr>
<td><code>int</code></td>
<td>

```yml
type: integer
format: int64
```

</td>
</tr>
<tr>
<td><code>int:Signed32</code></td>
<td>

```yml
type: integer
format: int32
```

</td>
</tr>
<tr>
<td>Other <code>int</code> types</td>
<td>

```yml
type: integer
```

</td>
</tr>
<tr>
<td><code>float</code></td>
<td>

```yml
type: number
format: float
```

</td>
</tr>
<tr>
<td><code>decimal</code></td>
<td>

```yml
type: number
format: double
```

</tr>
<tr>
<td><code>boolean</code></td>

<td>

```yml
type: boolean
```

</tr>
<tr>
<td><code>string</code></td>
<td>

```yml
type: string
```

</tr>
<tr>
<td><code>json</code></td>

<td>

```yml
type: object
```

</td>
</tr>
<tr>
<td><code>xml</code> and subtypes of <code>xml</code></td>
<td>

```yml
type: object
```

</td>
</tr>
<tr>
<td><code>byte</code></td>
<td>No mapping</td>
</tr>
<tr>
<td><code>()</code></td>
<td>

```yml
nullable: true
```

</td>
</tr>
<tr>
<td><code>anydata</code></td>
<td>

```yml
{}
```

</td>
</tr>
<tr>
<td><code>error</code></td>
<td>

```yml
type: object
properties:
 timestamp:
   type: string
 status:
   type: integer
   format: int64
 reason:
   type: string
 message:
   type: string
 path:
   type: string
 method:
   type: string
additionalProperties: false
```
(This is inferred from the error details defined in the Ballerina HTTP module)
</td>
</tr>
<tr>
<td><code>string:Char<code></td>
<td>

```yml
type: string
```

</td>
</tr>
<tr>
<td><code>enum</code></td>
<td>

```yml
type: string
enum:
 - ...
```

</tr>
</table>

#### Ballerina structural types

<table>
<tr>
<th>Structural Type</th>
<th>Mapping</th>
</tr>
<tr>
<td><code>array</code></td>
<td>

```yml
type: array
items:
   <element-type-schema>
```
</td>
</tr>
<tr>
<td><code>map<?></code></td>
<td>

```yml
type: object
additionalProperties:
   <element-type-schema>
```
</td>
</tr>
<tr>
<td><code>record{}</code></td>
<td>

```yml
type: object
allOf:
  - <from-type-inclusion>
  - type: object
    properties:
        <from-rest-of-the defined-members>
    additionalProperties:
        <from-rest-member-type>
```
</td>
</tr>
<tr>
<td><code>table</code></td>
<td>

```yml
type: object
additionalProperties:
   <element-type-schema>
```

</td>
</tr>
<tr>
<td><code>tuple</code></td>
<td>

```yml
type: array
properties:
  oneOf:
     <from-member-types>
```

</td>
</tr>
<tr>
<td><code>union</code></td>
<td>

```yml
oneOf:
 <from-member-types>
```

</td>
</tr>
<tr>
<td><code>intersection</code></td>
<td>

```yml
<from type after excluding readonly>
```

</td>
</tr>
</table>

### Ballerina constraints mapping to type schema

The Ballerina constraint package supports constraints on types. These constraints are mapped to the corresponding constraints in each type schema in the components section.

> **Note:** The constraint values are mapped to the type schema only if the value is directly provided in the constraint. If the constraint value is a variable, then the value is not mapped to the type schema.

#### Integer constraints

| Ballerina Mapping                               | Keywords           |
|-------------------------------------------------|--------------------|
| `@constraint:Int { minValue: <value>}`          | `minimum`          |
| `@constraint:Int { maxValue: <value>}`          | `maximum`          |
| `@constraint:Int { minValueExclusive: <value>}` | `exclusiveMinimum` |
| `@constraint:Int { maxValueExclusive: <value>}` | `exclusiveMaximum` |

Example:

```ballerina
import ballerina/http;
import ballerina/constraint;

@constraint:Int{
   minValue: 5,
   maxValue: 20
}

public type Age int;

type Person record{
   Age age?;
};

service / on new http:Listener(9090){
   resource function post newPerson(Person body) returns error?{
    return;
   }
}
```

```yml
components:
 schemas:
   Age:
       minimum: 5
       maximum: 20
       type: integer
       format: int32
   Person:
       type: object
       properties:
          age:
             $ref: '#/components/schemas/Age'
```

#### Float constraints

| Ballerina Mapping                                 | Keywords           |
|---------------------------------------------------|--------------------|
| `@constraint:Float { minValue: <value>}`          | `minimum`          |
| `@constraint:Float { maxValue: <value>}`          | `maximum`          |
| `@constraint:Float { minValueExclusive: <value>}` | `exclusiveMinimum` |
| `@constraint:Float { maxValueExclusive: <value>}` | `exclusiveMaximum` |

Example:

```ballerina
import ballerina/http;
import ballerina/constraint;

@constraint:Float{
   maxValue: 1000.5
}

public type Salary float;

type Person record{
   Salary salary?;
};

service / on new http:Listener(9090){
   resource function post newPerson(Person body) returns error?{
      return;
   }
}
```

```yml
components:
 schemas:
   Salary:
       maximum: 1000.5
       type: number
       format: float
   Person:
       type: object
       properties:
         salary:
           $ref: '#/components/schemas/Salary'
```

#### Number constraints

| Ballerina Mapping                                  | Keywords           |
|----------------------------------------------------|--------------------|
| `@constraint:Number { minValue: <value>}`          | `minimum`          |
| `@constraint:Number { maxValue: <value>}`          | `maximum`          |
| `@constraint:Number { minValueExclusive: <value>}` | `exclusiveMinimum` |
| `@constraint:Number { maxValueExclusive: <value>}` | `exclusiveMaximum` |

Example:

```ballerina
import ballerina/http;
import ballerina/constraint;

@constraint:Number{
   maxValueExclusive: 5.1
}

public type Rating decimal;

type Hotel record{
   Rating rate?;
};

service / on new http:Listener(9090){
   resource function post newHotel(Hotel body) returns error?{
     return;
   }
}
```

```yml
components:
 schemas:
   Rating:
       maximum: 5.1
       exclusiveMaximum: true
       type: number
       format: decimal
   Hotel:
       type: object
       properties:
         rate:
           $ref: '#/components/schemas/Rating'
```

#### String constraints

| Ballerina Mapping                          | Keywords                                 |
|--------------------------------------------|------------------------------------------|
| `@constraint:String { minLength: <value>}` | `minLength`                              |
| `@constraint:String { maxLength: <value>}` | `maxLength`                              |
| `@constraint:String { length: <value>}`    | `minLength: <value>, maxLength: <value>` |
| `@constraint:String { pattern: <value>}`   | `pattern`                                |

Example 1:

```ballerina
import ballerina/http;
import ballerina/constraint;

@constraint:String{
   minLength: 5,
   maxLength: 20,
   pattern: re `^[a-zA-Z0-9_]+$`
}

public type Username string;

type Person record{
   Username username?;
};

service / on new http:Listener(9090){
   resource function post newUser(Person body) returns error?{
     return;
   }
}
```

```yml
components:
 schemas:
   Username:
       minLength: 3
       maxLength: 20
       pattern: "^[a-zA-Z0-9_]+$"
       type: string
   Person:
       type: object
       properties:
         username:
           $ref: '#/components/schemas/Username'
```

Example 2:

```ballerina
import ballerina/http;
import ballerina/constraint;

@constraint:String{
   length: 5
}

public type ID string;

type Employee record{
   ID id;
};

service / on new http:Listener(9090){
   resource function post newEmployee(Employee body) returns error?{
     return;
   }
}
```

```yml
components:
 schemas:
   ID:
       minLength: 5
       maxLength: 5
       type: string
   Employee:
       type: object
       properties:
         id:
           $ref: '#/components/schemas/ID'
```

#### Array constraints

| Ballerina Mapping                         | Keywords                               |
|-------------------------------------------|----------------------------------------|
| `@constraint:Array { minLength: <value>}` | `minItems`                             |
| `@constraint:Array { maxLength: <value>}` | `maxItems`                             |
| `@constraint:Array { length: <value>}`    | `minItems: <value>, maxItems: <value>` |

Example:

```ballerina
import ballerina/http;
import ballerina/constraint;

@constraint:String{
   maxLength: 23
}
public type HobbyItemsString string;

@constraint:Array{
   minLength: 2,
   maxLength: 5
}
public type Hobby HobbyItemsString[];

public type person record{
   Hobby hobby?;
};

service / on new http:Listener(9090){
   resource function post hobby(Person body) returns error?{
        return;
   }
}
```

```yml
components:
 schemas:
   HobbyItemsString:
       maxLength: 23
       type: string
   Hobby:
       minLength: 2
       maxLength: 5
       type: array
       items:
          $ref: '#/components/schemas/HobbyItemsString'
   Person:
       type: object
       properties:
         hobby:
          $ref: '#/components/schemas/Hobby'
```

#### Date constraints

This is not yet supported in the OpenAPI tool.

```ballerina
@constraint:Date {
 option: "PAST"
}
type Date record {
   int year;
   int month;
   int day;
};
```

```console
WARNING Ballerina Date constraints might not be reflected in the OpenAPI definition
```
