# Specification: Ballerina OpenAPI Tool

_Owners_:  
_Reviewers_:  
_Created_:
_Updated_:   
_Edition_: Swan Lake


## Introduction

This is the specification for the Ballerina OpenAPI Tool of the [Ballerina language](https://ballerina.io/), which generates a Ballerina service or a client from an OpenAPI contract and vice versa.

The Open API Tool specification has evolved and may continue to evolve in the future. The released versions of the specification can be found under the relevant GitHub tag.

If you have any feedback or suggestions about the tool, start a discussion via a [GitHub issue](https://github.com/ballerina-platform/ballerina-standard-library/issues) or in the [Discord server](https://discord.gg/ballerinalang). Based on the outcome of the discussion, the specification and implementation can be updated. Community feedback is always welcome. Any accepted proposal, which affects the specification is stored under `/docs/proposals`. Proposals under discussion can be found with the label `type/proposal` in GitHub.

The conforming implementation of the specification is released and included in the distribution. Any deviation from the specification is considered a bug.

## Contents

- [Specification: Ballerina OpenAPI Tool](#specification-ballerina-openapi-tool)
    - [Introduction](#introduction)
    - [Contents](#contents)
    - [1. Overview](#1-overview)
    - [2. OpenAPI to Ballerina](#2-openapi-to-ballerina)
        - [2.1. Ballerina Client Generation](#21-ballerina-client-generation)
            - [2.1.1. Client Initialization](#211-client-initialization)
            - [2.1.2. Client Authentication](#212-client-authentication)
                - [2.1.2.1. HTTP Authentication](#2121-http-authentication)
                    - [Basic Authentication](#basic-authentication)
                    - [Bearer Authentication](#bearer-authentication)
                - [2.1.2.2. OAuth 2.0 Authentication](#2122-oauth-20-authentication)
                    - [Supporting multiple flows](#supporting-multiple-flows)
                - [2.1.2.3. API Keys](#2123-api-keys)
                - [Supporting multiple security schemes](#supporting-multiple-security-schemes)
            - [2.1.3. OpenAPI operation to Ballerina function mapping](#213-openapi-operation-to-ballerina-function-mapping)
                - [2.1.3.1. Resource Function vs Remote Function](#2131-resource-function-vs-remote-function)
                - [2.1.3.2. Parameters](#2132-parameters)
                    - [Path parameters](#path-parameters)
                    - [Query parameters](#query-parameters)
                    - [Header parameters](#header-parameters)
                    - [Cookie parameters](#cookie-parameters)
                - [2.1.3.2. Request Payload](#2132-request-payload)

## 1. Overview

This module provides the Ballerina OpenAPI tooling, which will make it easy to start the development of a service documented in an OpenAPI contract in Ballerina by generating the Ballerina service and client skeletons.

The OpenAPI tools provide the following capabilities.

1. Generate the Ballerina service or client code for a given OpenAPI definition.
2. Export the OpenAPI definition of a Ballerina service.
3. Validate the service implementation of a given OpenAPI contract.

The openapi command in Ballerina is used for OpenAPI to Ballerina and Ballerina to OpenAPI code generations. Code generation from OpenAPI to Ballerina can produce ballerina service stubs and ballerina client stubs. The OpenAPI compiler plugin will allow you to validate a service implementation against an OpenAPI contract during the compile time. This plugin ensures that the implementation of a service does not deviate from its OpenAPI contract.


## 2. OpenAPI to Ballerina

Generate Service and Client Stub from an OpenAPI Contract

```
bal openapi -i <openapi-contract-path> 
               [--tags: tags list]
               [--operations: operationsID list]
               [--mode service|client ]
               [(-o|--output): output file path]
```

Generates both the Ballerina service and Ballerina client stubs for a given OpenAPI file.

This `-i <openapi-contract-path>` parameter of the command is mandatory. It will get the path to the OpenAPI contract file (i.e., `my-api.yaml` or `my-api.json`) as an input.

You can give the specific tags and operations that you need to document as services without documenting all the operations using these optional `--tags` and `--operations` commands.

The `(-o|--output)` is an optional parameter. You can use this to give the output path of the generated files. If not, it will take the execution path as the output path.


**Modes**

If you want to generate a service only, you can set the mode as `service` in the OpenAPI tool.

```
bal openapi -i <openapi-contract-path> --mode service [(-o|--output) output file path]
```

If you want to generate a client only, you can set the mode as `client` in the OpenAPI tool. This client can be used in client applications to call the service defined in the OpenAPI file.


### 2.1. Ballerina Client Generation

Generates a Ballerina client from a given OpenAPI file. This include generating

```
bal openapi -i <openapi-contract-path> --mode client [(-o|--output) output file path]
```

By default the generated client name is “Client”.

#### 2.1.1. Client Initialization

The client init method has following parameters

1. Service URL (`string serviceUrl`)
- The server URL is identified from the `server` section of the given OpenAPI file.
- If there are multiple servers given, the first URL is used as the default service URL.
- If the server URL is parameterized, the default value for service URL is constructed using the default value of each variable.
    - Ex:
      ```yaml
      servers:
       url: https://{customerId}.saas-app.com:{port}/v2
       variables:
       customerId:
           default: demo
           description: Customer ID assigned by the service provider
       port:
           enum:
           - '443'
           - '8443'
           default: '443'
      ```
      The constructed default service URL for above OpenAPI server definition is `https://demo.saas-app.com:443/v2`

- Currently tool does not provide the support for [overriding servers](https://swagger.io/docs/specification/api-host-and-base-path/#:~:text=%C2%A0%C2%A0%C2%A0%C2%A0%C2%A0%C2%A0%C2%A0%C2%A0%C2%A0%20%2D%20southeastasia-,Overriding%20Servers,-The%20global%20servers)
- If there is no servers defined in the OpenAPI file, the parameter `serviceURL` is generated as a required parameter. Otherwise the `serviceURL` parameter is a defaultable parameter.

1. Client Configuration (`ConnectionConfig config`)
- `ConnectionConfig` record is based on the `http:ClientConfiguration`. The changes between the `http:ClientConfiguration` and `ConnectionConfig` is as below.
    - The type of the `auth` record is changed according to the `securitySchemes` defined in the OpenAPI file.
    - The `ClientHttp1Settings` record is generated within the client initializing default values for each fields.

Within the client init method, using the parameters provided by the user, an http:Client object is initialized to use across the client's resource/remote functions.

#### 2.1.2. Client Authentication

Defining the authentication mechanism of the client is handled at the client initialization stage. According to the `securitySchemes` defined in the OpenAPI file, the relevant ballerina HTTP authentication is mapped. User can provide the details related to the authentication in the `ConnectionConfig` record's `auth` field.

##### 2.1.2.1. HTTP Authentication

Under HTTP authentication schemes, `Basic` and `Bearer` mechanisms are supported in OpenAPI to Ballerina client generation.

###### Basic Authentication

Using OpenAPI 3.0, Basic authentication can be defined as below.

```yaml
openapi: 3.0.0
...
components:
  securitySchemes:
    basicAuth:     # <-- arbitrary name for the security scheme
      type: http
      scheme: basic
security:
  - basicAuth: []  # <-- use the same name here
```

By defining the auth field in the `ConnectionConfig` record as `http:CredentialsConfig` type, support for `Basic` authentication is enabled.

```bal
# Provides a set of configurations for controlling the behaviours when communicating with a remote HTTP endpoint.
@display {label: "Connection Config"}
public type ConnectionConfig record {|
    # Provides Auth configurations needed when communicating with a remote HTTP endpoint.
    http:CredentialsConfig auth;
    # The HTTP version understood by the client
    http:HttpVersion httpVersion = http:HTTP_2_0;
    # Configurations related to HTTP/1.x protocol
    ClientHttp1Settings http1Settings?;
    # Configurations related to HTTP/2 protocol
    http:ClientHttp2Settings http2Settings?;
    # The maximum time to wait (in seconds) for a response before closing the connection
    decimal timeout = 60;
    # The choice of setting `forwarded`/`x-forwarded` header
    string forwarded = "disable";
    # Configurations associated with request pooling
    http:PoolConfiguration poolConfig?;
    # HTTP caching related configurations
    http:CacheConfig cache?;
    # Specifies the way of handling compression (`accept-encoding`) header
    http:Compression compression = http:COMPRESSION_AUTO;
    # Configurations associated with the behaviour of the Circuit Breaker
    http:CircuitBreakerConfig circuitBreaker?;
    # Configurations associated with retrying
    http:RetryConfig retryConfig?;
    # Configurations associated with inbound response size limits
    http:ResponseLimitConfigs responseLimits?;
    # SSL/TLS-related options
    http:ClientSecureSocket secureSocket?;
    # Proxy server related options
    http:ProxyConfig proxy?;
    # Enables the inbound payload validation functionality which provided by the constraint package. Enabled by default
    boolean validation = true;
|};
```
User can declare the auth field as below along with other configuration related details to initialize a client with Basic authentication.

```bal
http:CredentialsConfig credentials = {
    username: "<your-username>",
    password: "<your-password>"
};
Client myClient = check new(config = {auth: credentials}, serviceUrl = "https://www.example.com");
```

###### Bearer Authentication

Using OpenAPI 3.0, Bearer authentication can be defined as below.

```yaml
openapi: 3.0.0
...
# 1) Define the security scheme type (HTTP bearer)
components:
  securitySchemes:
    bearerAuth:            # arbitrary name for the security scheme
      type: http
      scheme: bearer
# 2) Apply the security globally to all operations
security:
  - bearerAuth: []         # use the same name as above
```

By defining the auth field in the `ConnectionConfig` record as `http:BearerTokenConfig` type, support for `Bearer` authentication is enabled.

```bal
# Provides a set of configurations for controlling the behaviours when communicating with a remote HTTP endpoint.
@display {label: "Connection Config"}
public type ConnectionConfig record {|
    # Provides Auth configurations needed when communicating with a remote HTTP endpoint.
    http:CredentialsConfig auth;
    # The HTTP version understood by the client
    http:HttpVersion httpVersion = http:HTTP_2_0;
    # Configurations related to HTTP/1.x protocol
    ClientHttp1Settings http1Settings?;
    # Configurations related to HTTP/2 protocol
    http:ClientHttp2Settings http2Settings?;
    # The maximum time to wait (in seconds) for a response before closing the connection
    decimal timeout = 60;
    # The choice of setting `forwarded`/`x-forwarded` header
    string forwarded = "disable";
    # Configurations associated with request pooling
    http:PoolConfiguration poolConfig?;
    # HTTP caching related configurations
    http:CacheConfig cache?;
    # Specifies the way of handling compression (`accept-encoding`) header
    http:Compression compression = http:COMPRESSION_AUTO;
    # Configurations associated with the behaviour of the Circuit Breaker
    http:CircuitBreakerConfig circuitBreaker?;
    # Configurations associated with retrying
    http:RetryConfig retryConfig?;
    # Configurations associated with inbound response size limits
    http:ResponseLimitConfigs responseLimits?;
    # SSL/TLS-related options
    http:ClientSecureSocket secureSocket?;
    # Proxy server related options
    http:ProxyConfig proxy?;
    # Enables the inbound payload validation functionality which provided by the constraint package. Enabled by default
    boolean validation = true;
|};
```
User can declare the auth field as below along with other configuration related details to initialize a client with Bearer authentication.

```bal
http:BearerTokenConfig bearerConfig = { token: "<your-token>"};
Client testClient = check new(config = {auth: bearerConfig}, serviceUrl = "https://www.example.com");
```

When defining `Bearer` authentication in an OpenAPI file, the option to define `bearerFormat` is available. However, in the current OpenAPI to Ballerina client generation process, `bearerFormat` is not being taken into consideration.

##### 2.1.2.2. OAuth 2.0 Authentication

OAuth 2.0 is an authorization protocol that grants an API client restricted access to user data on a web server.

OAuth 2.0 defines various flows for obtaining an access token. For each OAuth 2.0 flow supported in the OpenAPI specification, the `auth` field of the `ConnectionConfig` record is generated accordingly, with the corresponding Ballerina HTTP grant configuration record.

Below table describes the mapping between the OpenAPI OAuth 2.0 flows and HTTP grant configuration.

| OpenAPI OAuth 2.0 authorization flow  | Ballerina HTTP grant configuration |
|----------|----------|
| authorizationCode | http:BearerTokenConfig OR http:OAuth2RefreshTokenGrantConfig  |
| implicit | http:BearerTokenConfig  |
| password | http:OAuth2PasswordGrantConfig  |
| clientCredentials | http:OAuth2ClientCredentialsGrantConfig |

In the `authorization code` flow apart from using the `http:OAuth2RefreshTokenGrantConfig` to get the access token internally, users can also provide the token obtained externally for authentication using `http:BearerTokenConfig`. Also the `refreshUrl` given under the `authorization code` flow is added as the default value of the `refreshUrl` field in the `OAuth2RefreshTokenGrantConfig`  record which is extended from `*http:OAuth2RefreshTokenGrantConfig`


```bal
# Provides a set of configurations for controlling the behaviours when communicating with a remote HTTP endpoint.
@display {label: "Connection Config"}
public type ConnectionConfig record {|
    # Configurations related to client authentication
    http:BearerTokenConfig|OAuth2RefreshTokenGrantConfig auth;
    # The HTTP version understood by the client
    http:HttpVersion httpVersion = http:HTTP_2_0;
    # Configurations related to HTTP/1.x protocol
    ClientHttp1Settings http1Settings?;
    # Configurations related to HTTP/2 protocol
    http:ClientHttp2Settings http2Settings?;
    # The maximum time to wait (in seconds) for a response before closing the connection
    decimal timeout = 60;
    # The choice of setting `forwarded`/`x-forwarded` header
    string forwarded = "disable";
    # Configurations associated with request pooling
    http:PoolConfiguration poolConfig?;
    # HTTP caching related configurations
    http:CacheConfig cache?;
    # Specifies the way of handling compression (`accept-encoding`) header
    http:Compression compression = http:COMPRESSION_AUTO;
    # Configurations associated with the behaviour of the Circuit Breaker
    http:CircuitBreakerConfig circuitBreaker?;
    # Configurations associated with retrying
    http:RetryConfig retryConfig?;
    # Configurations associated with inbound response size limits
    http:ResponseLimitConfigs responseLimits?;
    # SSL/TLS-related options
    http:ClientSecureSocket secureSocket?;
    # Proxy server related options
    http:ProxyConfig proxy?;
    # Enables the inbound payload validation functionality which provided by the constraint package. Enabled by default
    boolean validation = true;
|};

# OAuth2 Refresh Token Grant Configs
public type OAuth2RefreshTokenGrantConfig record {|
    *http:OAuth2RefreshTokenGrantConfig;
    # Refresh URL
    string refreshUrl = "<refresh-url>";
|};
```

In the `clientCredentials` flow, the `tokenUrl` given is added as the default value of the `tokenUrl` field of the `OAuth2ClientCredentialsGrantConfig` record which is extended from  `*http:OAuth2ClientCredentialsGrantConfig`.

```bal
# OAuth2 Client Credentials Grant Configs
public type OAuth2ClientCredentialsGrantConfig record {|
    *http:OAuth2ClientCredentialsGrantConfig;
    # Token URL
    string tokenUrl = "<token-url>";
|};
```

In the `password` flow also, the `tokenUrl` given is added as the default value of the `tokenUrl` field of the `OAuth2PasswordGrantConfig` record which is extended from  `*http:OAuth2PasswordGrantConfig`.

```bal
# OAuth2 Password Grant Configs
public type OAuth2PasswordGrantConfig record {|
    *http:OAuth2PasswordGrantConfig;
    # Token URL
    string tokenUrl = "<token-url>";
|};

```
In the current generation, the `scope` and `authorizationUrl` fields in the authorization flows does not take into consideration when generating the Ballerina client.

###### Supporting multiple flows

OAuth 2.0 security definition supports defining multiple flows within one security scheme.

```yaml
securitySchemes:
    oauth2_schemes:
      type: oauth2
      description: The security definitions for this API. Please check individual operations for applicable scopes.
      flows:
        clientCredentials:
          tokenUrl: '<token-url>'
          scopes:
            read: Grant read-only access to all your data except for the account and user info
        authorizationCode:
          authorizationUrl: '<authorization-url>'
          tokenUrl: '<token-url>'
          scopes:
            read: Grant read-only access to all your data except for the account and user info
```
In such scenarios an union type `auth` field is generated in the `ConnectionConfig` allowing users to use any of the flows defined. The `ConnectionConfig` record generated for above scenario is as below.

```bal
# Provides a set of configurations for controlling the behaviours when communicating with a remote HTTP endpoint.
@display {label: "Connection Config"}
public type ConnectionConfig record {|
    # Configurations related to client authentication
    OAuth2ClientCredentialsGrantConfig|http:BearerTokenConfig|OAuth2RefreshTokenGrantConfig auth;
    # The HTTP version understood by the client
    http:HttpVersion httpVersion = http:HTTP_2_0;
    # Configurations related to HTTP/1.x protocol
    ClientHttp1Settings http1Settings?;
    # Configurations related to HTTP/2 protocol
    http:ClientHttp2Settings http2Settings?;
    # The maximum time to wait (in seconds) for a response before closing the connection
    decimal timeout = 60;
    # The choice of setting `forwarded`/`x-forwarded` header
    string forwarded = "disable";
    # Configurations associated with request pooling
    http:PoolConfiguration poolConfig?;
    # HTTP caching related configurations
    http:CacheConfig cache?;
    # Specifies the way of handling compression (`accept-encoding`) header
    http:Compression compression = http:COMPRESSION_AUTO;
    # Configurations associated with the behaviour of the Circuit Breaker
    http:CircuitBreakerConfig circuitBreaker?;
    # Configurations associated with retrying
    http:RetryConfig retryConfig?;
    # Configurations associated with inbound response size limits
    http:ResponseLimitConfigs responseLimits?;
    # SSL/TLS-related options
    http:ClientSecureSocket secureSocket?;
    # Proxy server related options
    http:ProxyConfig proxy?;
    # Enables the inbound payload validation functionality which provided by the constraint package. Enabled by default
    boolean validation = true;
|};

# OAuth2 Client Credentials Grant Configs
public type OAuth2ClientCredentialsGrantConfig record {|
    *http:OAuth2ClientCredentialsGrantConfig;
    # Token URL
    string tokenUrl = "https://api.ebay.com/identity/v1/oauth2/token";
|};

# OAuth2 Refresh Token Grant Configs
public type OAuth2RefreshTokenGrantConfig record {|
    *http:OAuth2RefreshTokenGrantConfig;
    # Refresh URL
    string refreshUrl = "https://api.ebay.com/identity/v1/oauth2/token";
|};
```

Note that `type: openIdConnect` security schemes are not yet supported in the OpenAPI to Ballerina client generation.

##### 2.1.2.3. API Keys

Some APIs use API keys for authorization. An API key is a token that a client provides when making API calls. This key can be sent in the query string, as a request header, or as a cookie. Currently, in the OpenAPI to Ballerina client generation process, the scenarios involving the query string and request header are supported.

In such use case, apart from the `config` and `serviceUrl` parameters, `apiKeyConfig` parameter is added to the client's init function signature allowing users to provide APIKeys needed at the time of client initializing.

```bal
# Gets invoked to initialize the `connector`.
# The connector initialization requires setting the API credentials.
#
# + apiKeyConfig - API keys for authorization 
# + config - The configurations to be used when initializing the `connector` 
# + serviceUrl - URL of the target service 
# + return - An error if connector initialization failed 
public isolated function init(ApiKeysConfig apiKeyConfig, ConnectionConfig config =  {}, string serviceUrl = "http://example.org/") returns error? {
    http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, timeout: config.timeout, forwarded: config.forwarded, poolConfig: config.poolConfig, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, validation: config.validation};
    do {
        if config.http1Settings is ClientHttp1Settings {
            ClientHttp1Settings settings = check config.http1Settings.ensureType(ClientHttp1Settings);
            httpClientConfig.http1Settings = {...settings};
        }
        if config.http2Settings is http:ClientHttp2Settings {
            httpClientConfig.http2Settings = check config.http2Settings.ensureType(http:ClientHttp2Settings);
        }
        if config.cache is http:CacheConfig {
            httpClientConfig.cache = check config.cache.ensureType(http:CacheConfig);
        }
        if config.responseLimits is http:ResponseLimitConfigs {
            httpClientConfig.responseLimits = check config.responseLimits.ensureType(http:ResponseLimitConfigs);
        }
        if config.secureSocket is http:ClientSecureSocket {
            httpClientConfig.secureSocket = check config.secureSocket.ensureType(http:ClientSecureSocket);
        }
        if config.proxy is http:ProxyConfig {
            httpClientConfig.proxy = check config.proxy.ensureType(http:ProxyConfig);
        }
    }
    http:Client httpEp = check new (serviceUrl, httpClientConfig);
    self.clientEp = httpEp;
    self.apiKeyConfig = apiKeyConfig.cloneReadOnly();
    return;
}
```

The `APIKeysConfig` record generated including all the APIKeys as fields.

```bal
# Provides API key configurations needed when communicating with a remote HTTP endpoint.
public type ApiKeysConfig record {|
    # Represents API Key `appid`
    @display {label: "", kind: "password"}
    string appid;
|};
```

In the scenario where the API key needs to be sent in the query string, the API key is added to the query string along with other parameters within each resource/remote function.

```bal
# Access current weather data for any location.
#
# + id - City ID. Example: `2172797`. The List of city IDs can be downloaded [here](http://bulk.openweathermap.o/sample/).

# + return - Current weather data of the given location 
@display {label: "Current Weather"}
resource isolated function get weather(@display {label: "City Id"} string? id = ()) returns CurrentWeatherData|error {
    string resourcePath = string `/weather`;
    map<anydata> queryParam = {"id": id, "appid": self.apiKeyConfig.appid};
    resourcePath = resourcePath + check getPathForQueryParam(queryParam);
    CurrentWeatherData response = check self.clientEp->get(resourcePath);
    return response;
}
```

In the scenario where the API key needs to be sent in the request header, the API key is added to the entity header map along with other headers within each resource/remote function.

```bal
    # List Events
    #
    # + filterTitle - Filter events by title 
    # + filterEveryoneCanSpeak - Filter events by everyone can speak 
    # + return - Fetch List 
    @display {label: "Get List Of Events"}
    remote isolated function listEvents(@display {label: "Title Filter"} string? filterTitle = (), @display {label: "Filter By Everyone Can Speak Or Not"} string? filterEveryoneCanSpeak = ()) returns InlineResponse200|error {
        string resourcePath = string `/events`;
        map<anydata> queryParam = {"filterTitle": filterTitle, "filterEveryoneCanSpeak": filterEveryoneCanSpeak};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<any> headerValues = {"Authorization": self.apiKeyConfig.authorization};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        InlineResponse200 response = check self.clientEp->get(resourcePath, httpHeaders);
        return response;
    }
```

##### Supporting multiple security schemes

There can be scenarios where multiple security schemes: API Key + HTTP + OAuth2 are defined together in a OpenAPI file

```yaml
  securitySchemes:
    oauth2_scheme:
      type: oauth2
      flows:
        implicit:
          authorizationUrl: https://petstore3.swagger.io/oauth/authorize
          scopes:
            write:pets: modify pets in your account
            read:pets: read your pets
    api_key:
      type: apiKey
      name: api_key
      in: header
```

In such cases, the `auth` field of the `ConnectionConfig `record is generated as a union type, and the client is initialized with the appropriate authentication mechanism at runtime.

#### 2.1.3. OpenAPI operation to Ballerina function mapping

In OpenAPI terms, paths are endpoints (resources), such as `/users` or `/reports/summary/`, that your API exposes, and operations are the HTTP methods used to manipulate these paths, such as GET, POST, PUT or DELETE.

A remote or resource method is generated for each operation in every path. By default, the functions generated inside the client class are resource methods. However, users have the option to specify the client method type using the command option `--client-methods <remote|resource>`. This allows users to define whether the client method type should be resource or remote.


##### 2.1.3.1. Resource Function vs Remote Function

The difference between the resource function and remote function in OpenAPI to Ballerina client generation is only in the method signature.

Following are the example resource and remote methods generated for the same endpoint operation.

**Sample OpenAPI operation**

```yaml
  /user/{username}:
    put:
      tags:
        - user
      summary: Update user
      description: This can only be done by the logged in user.
      operationId: updateUser
      parameters:
        - name: username
          in: path
          description: name that need to be deleted
          required: true
          schema:
            type: string
      requestBody:
        description: Update an existent user in the store
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/User'
          application/xml:
            schema:
              $ref: '#/components/schemas/User'
          application/x-www-form-urlencoded:
            schema:
              $ref: '#/components/schemas/User'
      responses:
        default:
          description: successful operation
```

**Generated resource method**

```bal
  # Update user
  #
  # + username - name that need to be deleted
  # + payload - Update an existent user in the store
  # + return - successful operation 
  resource isolated function put user/[string username](User payload) returns http:Response|error {
      string resourcePath = string `/user/${getEncodedUri(username)}`;
      http:Request request = new;
      json jsonBody = payload.toJson();
      request.setPayload(jsonBody, "application/json");
      http:Response response = check self.clientEp->put(resourcePath, request);
      return response;
  }
```

"The function name `put` is derived from the corresponding HTTP method specified in the OpenAPI operation. The relative resource path `user/[string username]` is constructed based on the `path`. In this case, `username` is a path parameter defined under the `parameters` section of the operation. For additional information on mapping parameters from OpenAPI to Ballerina, please refer to the [parameter]() section."

**Generated remote method**


```bal
  # Update user
  #
  # + username - name that need to be deleted
  # + payload - Update an existent user in the store
  # + return - successful operation 
  remote isolated function updateUser(string username, User payload) returns http:Response|error {
      string resourcePath = string `/user/${getEncodedUri(username)}`;
      http:Request request = new;
      json jsonBody = payload.toJson();
      request.setPayload(jsonBody, "application/json");
      http:Response response = check self.clientEp->put(resourcePath, request);
      return response;
  }
```

The remote method derives its name from the operationId defined in the OpenAPI file, which serves as a unique identifier for each operation. Although this field is not mandatory in the OpenAPI specification, it is necessary to provide a unique operationId for each operation when generating a Ballerina client with remote methods.

##### 2.1.3.2. Parameters

In OpenAPI 3.0, parameters are defined in the parameters section of an operation or path. Here is an example:

```yaml
paths:
  /users/{userId}:
    get:
      summary: Get a user by ID
      parameters:
        - in: path
          name: userId
          schema:
            type: integer
          required: true
          description: Numeric ID of the user to get
```

The name of the parameter generated in the Ballerina method signature is derived from the `name` attribute in the parameters section of the OpenAPI file. If the name does not conform to the standard Ballerina parameter naming conventions, it is formatted to adhere to the standard.

The `in` attribute in the parameters section specifies whether the parameter should be included as a path parameter, query parameter, header parameter, or cookie parameter. Currently, in the OpenAPI to Ballerina client generation process, all parameters except for cookie parameters are supported.

The type of the parameter is determined from the `schema` attribute, and the value specified for the `required` attribute determines whether a parameter is mandatory or optional.


###### Path parameters

Path parameters are always considered required parameters. In the implementation of the resource method, the path parameter is added to the method signature, while in the implementation of the remote method, the path parameter is added as a method parameter. The process of appending the path parameter to the resource path remains the same. Before appending the path parameter, UTF-8 encoding is applied to avoid issues with unsupported characters.

The path parameter types supported in Ballerina are `string`, `int`, `float`, `boolean` and `decimal`. Therefore any other type other than these type would not be supported in OpenAPI to Ballerina client generation including `array` type and `object` types. The following is the mapping of the supported Ballerina data types to OpenAPI parameter schemas.

| Ballerina type  | OpenAPI parameter type |
|----------|----------|
| string | `type: string`  |
| int | `type: `integer`  |
| float | `type: number; format: float`  |
| decimal | `type: number; format: double` or format not given |

Following are few specific path parameter related scenarios.

**Scenario 1**

If a reference to any of the above supported types is provided as the type of the path parameter, a path parameter with a type reference is generated.

_Sample OpenAPI snippet_

```yaml
paths:
  /v1/{id}:
    get:
      operationId: operationId03
      parameters:
        - name: id
          description: "id value"
          in: path
          required: true
          schema:
            $ref: "#/components/schemas/Id"
      responses:
        "200":
          description: Ok
          content:
            text/plain:
              schema:
                type: string
components:
  schemas:
    Id:
      type: integer
```

_Generated resource method_

```bal
public type Id int;
...
  
  remote isolated function operationId03(Id id) returns string|error {
      string resourcePath = string `/v1/${getEncodedUri(id)}`;
      string response = check self.clientEp-> get(resourcePath);
      return response;
  }
...
```

**Scenario 2**

It is important to note that having a path parameter with `nullable: true` is not valid in Ballerina and will result in an error.

_Sample OpenAPI snippet_

```yaml
  parameters:
    - description: "Department name"
      in: path
      name: department
      schema:
        type: string
        nullable: true
      required: true
```

**Scenario 3**

If a default value is given for a parameter, it will be ignored when generating the client since in Ballerina path parameter should always be a required parameter.

_Sample OpenAPI snippet_

```yaml
  parameters:
    - description: "Department name"
      in: path
      name: department
      schema:
        type: string
        default: HR
      required: true
```

###### Query parameters

According to OpenAPI specification query parameters can be primitive values, arrays and objects. The supported query parameter types in Ballerina are `string`, `int`, `float`, `boolean`, `decimal`, and the array types of the aforementioned types. The query param type can be nil as well.  Currently due to the limitation in Ballerina, the object type query parameters are not supported in OpenAPI to Ballerina client generation.

"The query parameters specified in the OpenAPI file are included as parameters for the resource or remote method. Below is an example of a resource function generated with query parameters named `name` and `status`."

_Sample OpenAPI snippet_

```yaml    
  /pet/{petId}:
    get:
      tags:
        - pet
      summary: Get details if a pet 
      description: ''
      operationId: getPetById
      parameters:
        - name: petId
          in: path
          description: ID of pet
          required: true
          schema:
            type: integer
            format: int64
        - name: name
          in: query
          description: Name of pet that needs to be retrieved
          schema:
            type: string
        - name: status
          in: query
          description: Status of pet that needs to be retrieved
          schema:
            type: string
      responses:
        '200':
          description: successful operation
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Pet' 
```

_Generated resource function_

```bal
  resource isolated function get pet/[int petId](string? name = (), string? status = ()) returns Pet|error {
      string resourcePath = string `/pet/${getEncodedUri(petId)}`;
      map<anydata> queryParam = {"name": name, "status": status};
      resourcePath = resourcePath + check getPathForQueryParam(queryParam);
      Pet response = check self.clientEp->get(resourcePath);
      return response;
  } 
```

As demonstrated in the above example, within the function body, the resource path is enhanced with these parameters by employing the utility function `getPathForQueryParam`. Additionally, before appending to the resource path, the `getPathForQueryParam` function also performs the serialization and UTF-8 encoding of query parameters.

When a query parameter is defined in an OpenAPI file with the attribute `required: true`, a mandatory parameter is generated in Ballerina. If the parameter is not required, it is generated as a `defaultable parameter`. In cases where a default value is specified for a particular query parameter in the OpenAPI file, that value is used as the default value for the corresponding Ballerina parameter.

If no default value is provided, the parameter is generated with `nil` as the default. In the example below, the parameter `name` is required, the parameter `status` has a default value, and the parameter `category` is an optional parameter with no default value."

_Sample OpenAPI snippet_
```bal
  parameters:
    - name: petId
      in: path
      description: ID of pet that needs to be updated
      required: true
      schema:
        type: integer
        format: int64
    - name: name
      in: query
      description: Name of pet that needs to be updated
      schema:
        type: string
    - name: status
      in: query
      description: Status of pet that needs to be updated
      schema:
        type: string
        default: "pending"
    - name: category
      in: query
      schema: 
        type: string
      description: Type of the pet
```


_Generated resource function_

```bal
  resource isolated function get pet(string? name = (), string status = "pending", string? category = ()) returns Pet|error {
      ...
  }
```

If a reference to any of the above supported types is provided as the type of the query parameter, a query parameter with a type reference is generated.

###### Header parameters

An API call may require that custom headers be sent with an HTTP request. In a OpenAPI file, custom request headers can be defined as `in: header` parameters. Here is an example.

```yaml
paths:
  /ping:
    get:
      summary: Checks if the server is alive
      parameters:
        - in: header
          name: X-Request-ID
          schema:
            type: string
            format: uuid
          required: true
```

According to OpenAPI specification header parameters can be primitive values, arrays and objects. The supported header parameter types in Ballerina are primitive types (`string`, `int`, `float`, `boolean`, `decimal`), and the array types of the primitive types and records opened by any of the primitive types. In the OpenAPI to Ballerina client generation only the primitive type headers are supported. The array and object type header values are not supported.

###### Cookie parameters

Operations can also pass parameters in the Cookie header as Cookie: name=value. However, in the OpenAPI to Ballerina client generation, cookie parameters are currently not taken into consideration.

##### 2.1.3.2. Request Payload

Request bodies are commonly utilized in operations such as `create` and `update` (POST, PUT, PATCH). For instance, when creating a resource through `POST` or `PUT`, the request body typically carries the representation of the resource to be created. In the OpenAPI specification, the `requestBody` keyword is provided to describe these request bodies.

OpenAPI 3.0 uses the `requestBody` keyword to distinguish the payload from parameters (such as query string). The requestBody is more flexible in that it lets you consume different media types, such as `JSON`, `XML`, `form data`, `plain text`, and others, and use different schemas for different media types. requestBody consists of the `content` object, an optional Markdown-formatted `description`, and an optional `required` flag (false by default). Content lists the media types consumed by the operation (such as `application/json`) and specifies the schema for each media type. Here is an example.

```yaml
paths:
  /pets:
    post:
      summary: Add a new pet
      requestBody:
        description: Optional description in *Markdown*
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Pet'
          application/xml:
            schema:
              $ref: '#/components/schemas/Pet'
          application/x-www-form-urlencoded:
            schema:
              $ref: '#/components/schemas/PetForm'
          text/plain:
            schema:
              type: string
      responses:
        '201':
          description: Created
```

Following are the scenario supported in OpenAPI to Ballerina client generation.

**Scenario 1 : Payload with referenced schema**

_Sample OpenAPI snippet_

```yaml
paths:
  /pet:
    put:
      tags:
        - pet
      summary: Update an existing pet
      description: Update an existing pet by Id
      operationId: updatePet
      requestBody:
        description: Update an existent pet in the store
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Pet'
components: 
  schemas: 
    Pet:
      type: object
      properties:
        id:
          type: integer
          format: int64
          example: 10
        name:
          type: string
          example: doggie
```

_Generated resource function_

```bal
    # Update an existing pet
    #
    # + payload - Update an existent pet in the store
    # + return - Successful operation 
    resource isolated function put pet(Pet payload) returns Pet|error {
        string resourcePath = string `/pet`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        Pet response = check self.clientEp->put(resourcePath, request);
        return response;
    }
```

Here the type of the payload is `Pet` which is a reference to a Ballerina type.


**Scenario 2 : Payload with inline schema**

_Sample OpenAPI snippet_

```yaml
/pet:
  put:
    tags:
      - pet
    summary: Update an existing pet
    description: Update an existing pet by Id
    operationId: updatePet
    requestBody:
      description: Update an existent pet in the store
      content:
        application/json:
          schema:
            type: object
            properties:
              id:
                type: integer
                format: int64
                example: 10
              name:
                type: string
                example: doggie
```

_Generated resource function_

```bal
  # Update an existing pet
  #
  # + payload - Update an existent pet in the store
  # + return - Successful operation 
  resource isolated function put pet(Pet_body payload) returns Pet|error {
      string resourcePath = string `/pet`;
      http:Request request = new;
      json jsonBody = payload.toJson();
      request.setPayload(jsonBody, "application/json");
      Pet response = check self.clientEp->put(resourcePath, request);
      return response;
  }
```

Here the Ballerina type `Pet_body` is created although it is not defined as a schema in the original OpenAPI file. Below is the generated `Pet_body` record.

```bal
public type Pet_body record {
    int id?;
    string name?;
};
```

**Scenario 3 : application/json content type**

As given in above scenario 1 and scenario 2 examples when the content type is set as `application/json`, the `payload` parameter value is converted to JSON using the `toJson()` method and then assigned to the payload with the content type `application/json`."

When an empty schema given for `application/json` content-type request body, the resource function is generated as below.

```bal
    # Update an existing pet
    #
    # + payload - Update an existent pet in the store
    # + return - Successful operation 
    resource isolated function put pet(json payload) returns Pet|error {
        string resourcePath = string `/pet`;
        http:Request request = new;
        request.setPayload(payload, "application/json");
        Pet response = check self.clientEp->put(resourcePath, request);
        return response;
    }
```

The type of the `payload` parameter is `json`.

**Scenario 3 : application/xml content type**

_Sample OpenAPI snippet_

```yaml
  /pet:
    put:
      tags:
        - pet
      summary: Update an existing pet
      description: Update an existing pet by Id
      operationId: updatePet
      requestBody:
        description: Update an existent pet in the store
        content:
          application/xml:
            schema:
              $ref: '#/components/schemas/Pet'
```

_Generated resource function_

```bal
  # Update an existing pet
  #
  # + payload - Update an existent pet in the store
  # + return - Successful operation 
  resource isolated function put pet(Pet payload) returns Pet|error {
      string resourcePath = string `/pet`;
      http:Request request = new;
      json jsonBody = payload.toJson();
      xml? xmlBody = check xmldata:fromJson(jsonBody);
      request.setPayload(xmlBody, "application/xml");
      Pet response = check self.clientEp->put(resourcePath, request);
      return response;
  }
```
