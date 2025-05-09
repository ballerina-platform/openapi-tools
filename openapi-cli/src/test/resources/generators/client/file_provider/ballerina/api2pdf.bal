import ballerina/http;
import ballerina/data.jsondata;

# Client endpoint for the given API
public isolated client class Client {
    final http:Client clientEp;
    final readonly & ApiKeysConfig apiKeyConfig;
    # Gets invoked to initialize the `connector`.
    #
    # + apiKeyConfig - API keys for authorization
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ApiKeysConfig apiKeyConfig, string serviceUrl, ConnectionConfig config =  {}) returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
        self.apiKeyConfig = apiKeyConfig.cloneReadOnly();
    }

    # Convert raw HTML to PDF
    #
    # + headers - Headers to be sent with the request
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function chromeFromHtmlPost(ChromeHtmlToPdfRequest payload, map<string|string[]> headers = {}) returns ApiResponseSuccess|error {
        string resourcePath = string `/chrome/html`;
        map<anydata> headerValues = {...headers};
        headerValues["Authorization"] = self.apiKeyConfig.Authorization;
        map<string|string[]> httpHeaders = http:getHeaderMap(headerValues);
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request, httpHeaders);
    }

    # Convert URL to PDF
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - A PDF file or a JSON object depending on the `output` query parameter
    remote isolated function chromeFromUrlGET(map<string|string[]> headers = {}, *ChromeFromUrlGETQueries queries) returns ApiResponseSuccess|error {
        string resourcePath = string `/chrome/url`;
        map<anydata> queryParam = {...queries};
        queryParam["apikey"] = self.apiKeyConfig.apikey;
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp->get(resourcePath, headers);
    }

    # Convert URL to PDF
    #
    # + headers - Headers to be sent with the request
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function chromeFromUrlPost(ChromeUrlToPdfRequest payload, map<string|string[]> headers = {}) returns ApiResponseSuccess|error {
        string resourcePath = string `/chrome/url`;
        map<anydata> headerValues = {...headers};
        headerValues["Authorization"] = self.apiKeyConfig.Authorization;
        map<string|string[]> httpHeaders = http:getHeaderMap(headerValues);
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request, httpHeaders);
    }

    # Convert office document or image to PDF
    #
    # + headers - Headers to be sent with the request
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function libreConvertPost(LibreOfficeConvertRequest payload, map<string|string[]> headers = {}) returns ApiResponseSuccess|error {
        string resourcePath = string `/libreoffice/convert`;
        map<anydata> headerValues = {...headers};
        headerValues["Authorization"] = self.apiKeyConfig.Authorization;
        map<string|string[]> httpHeaders = http:getHeaderMap(headerValues);
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request, httpHeaders);
    }

    # Merge multiple PDFs together
    #
    # + headers - Headers to be sent with the request
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function mergePost(MergeRequest payload, map<string|string[]> headers = {}) returns ApiResponseSuccess|error {
        string resourcePath = string `/merge`;
        map<anydata> headerValues = {...headers};
        headerValues["Authorization"] = self.apiKeyConfig.Authorization;
        map<string|string[]> httpHeaders = http:getHeaderMap(headerValues);
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request, httpHeaders);
    }

    # Convert raw HTML to PDF
    #
    # + headers - Headers to be sent with the request
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function wkhtmltopdfFromHtmlPost(WkHtmlToPdfHtmlToPdfRequest payload, map<string|string[]> headers = {}) returns ApiResponseSuccess|error {
        string resourcePath = string `/wkhtmltopdf/html`;
        map<anydata> headerValues = {...headers};
        headerValues["Authorization"] = self.apiKeyConfig.Authorization;
        map<string|string[]> httpHeaders = http:getHeaderMap(headerValues);
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request, httpHeaders);
    }

    # Convert URL to PDF
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - A PDF file or a JSON object depending on the `output` query parameter
    remote isolated function wkhtmltopdfFromUrlGET(map<string|string[]> headers = {}, *WkhtmltopdfFromUrlGETQueries queries) returns ApiResponseSuccess|error {
        string resourcePath = string `/wkhtmltopdf/url`;
        map<anydata> queryParam = {...queries};
        queryParam["apikey"] = self.apiKeyConfig.apikey;
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp->get(resourcePath, headers);
    }

    # Convert URL to PDF
    #
    # + headers - Headers to be sent with the request
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function wkhtmltopdfFromUrlPost(WkHtmlToPdfUrlToPdfRequest payload, map<string|string[]> headers = {}) returns ApiResponseSuccess|error {
        string resourcePath = string `/wkhtmltopdf/url`;
        map<anydata> headerValues = {...headers};
        headerValues["Authorization"] = self.apiKeyConfig.Authorization;
        map<string|string[]> httpHeaders = http:getHeaderMap(headerValues);
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request, httpHeaders);
    }

    # Generate bar codes and QR codes with ZXING.
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - An image of the generated barcode or QR code
    remote isolated function zebraGET(map<string|string[]> headers = {}, *ZebraGETQueries queries) returns byte[]|error {
        string resourcePath = string `/zebra`;
        map<anydata> queryParam = {...queries};
        queryParam["apikey"] = self.apiKeyConfig.apikey;
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp->get(resourcePath, headers);
    }
}
