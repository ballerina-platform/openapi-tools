import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "https://www.formstack.com/api/v2") returns error? {
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
        return;
    }
    #
    # + return - Successful response
    resource isolated function post v4/spreadsheets/[string spreadsheetId]/sheets/[int sheetId]/copyTo() returns string|error {
        string resourcePath = string `/v4/spreadsheets/${getEncodedUri(spreadsheetId)}/sheets/${getEncodedUri(sheetId)}:copyTo`;
        http:Request request = new;
        string response = check self.clientEp-> post(resourcePath, request);
        return response;
    }
    #
    # + return - Successful response
    resource isolated function post v4/spreadsheets/[string spreadsheetId]/sheets/[int sheetId]/copyFrom() returns string|error {
        string resourcePath = string `/v4/spreadsheets/${getEncodedUri(spreadsheetId)}/sheets/${getEncodedUri(sheetId)}:copyFrom`;
        http:Request request = new;
        string response = check self.clientEp-> post(resourcePath, request);
        return response;
    }
    #
    # + return - Successful response
    resource isolated function post v4/spreadsheets/[string spreadsheetId]/[int sheetId]/sheets/[int sheetId]/copyTo() returns string|error {
        string resourcePath = string `/v4/spreadsheets/${getEncodedUri(spreadsheetId)}.${getEncodedUri(sheetId)}/sheets/${getEncodedUri(sheetId)}:copyTo`;
        http:Request request = new;
        string response = check self.clientEp-> post(resourcePath, request);
        return response;
    }
    #
    # + return - Successful response
    resource isolated function post payroll/v1/workers/[string associateoid]/organizational\-pay\-statements/[int payStatementId]/images/[int imageId]/[string imageExtension]() returns string|error {
        string resourcePath = string `/payroll/v1/workers/${getEncodedUri(associateoid)}/organizational-pay-statements/${getEncodedUri(payStatementId)}/images/${getEncodedUri(imageId)}.${getEncodedUri(imageExtension)}`;
        http:Request request = new;
        string response = check self.clientEp-> post(resourcePath, request);
        return response;
    }
    #
    # + return - Successful response
    resource isolated function post v3/ClientGroups/GetClientGroupByUserDefinedIdentifier/UserDefinedIdentifier/[string userDefinedIdentifier]() returns string|error {
        string resourcePath = string `/v3/ClientGroups/GetClientGroupByUserDefinedIdentifier(UserDefinedIdentifier='${getEncodedUri(userDefinedIdentifier)}')`;
        http:Request request = new;
        string response = check self.clientEp-> post(resourcePath, request);
        return response;
    }
    #
    # + return - Successful response
    resource isolated function post companies/[string company_id]/items/[int item_id]() returns string|error {
        string resourcePath = string `/companies(${getEncodedUri(company_id)})/items(${getEncodedUri(item_id)})`;
        http:Request request = new;
        string response = check self.clientEp-> post(resourcePath, request);
        return response;
    }
}
