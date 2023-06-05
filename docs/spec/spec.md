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

1. [Overview](#1-overview)
2. [OpenAPI to Balerina]()
    * 2.1. [Ballerina Client Generation]()
        * 2.1.1. [Client Initialization]()
        * 2.1.2. [Client Authentication]()
            * [HTTP Authentication Schemes]()
            * [API Keys]()
            * [OAuth2]()
        * 2.1.3. [OpenAPI operation to Ballerina function mapping]()
            * 2.1.3.1 [Resource Function vs Remote Function]()
            * 2.1.3.2 [Parameters]()
                * [Path parameters]()
                * [Query Parameters]()
                * [Header parameters]()
                * [Cookie parameters]()
                * [Parameter serialization]()
                * [Required parameters]()
                * [Optional parameters]()
            * 2.1.3.3. [Request payload]()
            * 2.1.3.4. [Return type]()
    * 2.2. [Ballerina Type Generation]()
        * 2.2.1 [Primitive types]()
        * 2.2.2 [Arrays]()
        * 2.2.3 [Object to Record mapping]()
        * 2.2.4 [oneOf, anyOf, allOf, not]()
        * 2.2.5 [Enums]()
        * 2.2.6 [JSON schema keywords]()
            * [pattern]()
            * [nullable]()
    * 2.3. [Ballerina Service Generation]()
        * 2.3.1. [Listener]()
        * 2.3.2. [Service]()
            * 2.3.2.1 [Resource function]()
            * 2.3.2.2 [Parameters]()
                * [Path parameters]()
                * [Query Parameters]()
                * [Header parameters]()
                * [Cookie parameters]()
                * [Required parameters]()
                * [Optional parameters]()
            * 2.1.3.3. [Request payload]()
            * 2.1.3.4. [Return type]()
3. [OpenAPI to Ballerina]()

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








