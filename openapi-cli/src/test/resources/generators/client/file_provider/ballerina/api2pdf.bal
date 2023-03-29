import ballerina/http;

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
     public isolated function init(ApiKeysConfig apiKeyConfig, string serviceUrl, ConnectionConfig config = {}) returns error? {
        http:ClientConfiguration httpClientConfig = {
                    httpVersion: config.httpVersion,
                    timeout: config.timeout,
                    forwarded: config.forwarded,
                    poolConfig: config.poolConfig,
                    compression: config.compression,
                    circuitBreaker: config.circuitBreaker,
                    retryConfig: config.retryConfig,
                    validation: config.validation
        };
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
    # Convert raw HTML to PDF
    #
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function chromeFromHtmlPost(ChromeHtmlToPdfRequest payload) returns ApiResponseSuccess|error {
        string resourcePath = string `/chrome/html`;
        map<any> headerValues = {"Authorization": self.apiKeyConfig.authorization};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        ApiResponseSuccess response = check self.clientEp->post(resourcePath, request, httpHeaders);
        return response;
    }
    # Convert URL to PDF
    #
    # + url - Url of the page to convert to PDF. Must start with http:// or https://.
    # + output - Specify output=json to receive a JSON output. Defaults to PDF file.
    # + return - A PDF file or a JSON object depending on the `output` query parameter
    remote isolated function chromeFromUrlGET(string url, string? output = ()) returns ApiResponseSuccess|error {
        string resourcePath = string `/chrome/url`;
        map<anydata> queryParam = {"url": url, "output": output, "apikey": self.apiKeyConfig.apikey};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        ApiResponseSuccess response = check self.clientEp-> get(resourcePath);
        return response;
    }
    # Convert URL to PDF
    #
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function chromeFromUrlPost(ChromeUrlToPdfRequest payload) returns ApiResponseSuccess|error {
        string resourcePath = string `/chrome/url`;
        map<any> headerValues = {"Authorization": self.apiKeyConfig.authorization};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        ApiResponseSuccess response = check self.clientEp->post(resourcePath, request, httpHeaders);
        return response;
    }
    # Convert office document or image to PDF
    #
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function libreConvertPost(LibreOfficeConvertRequest payload) returns ApiResponseSuccess|error {
        string resourcePath = string `/libreoffice/convert`;
        map<any> headerValues = {"Authorization": self.apiKeyConfig.authorization};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        ApiResponseSuccess response = check self.clientEp->post(resourcePath, request, httpHeaders);
        return response;
    }
    # Merge multiple PDFs together
    #
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function mergePost(MergeRequest payload) returns ApiResponseSuccess|error {
        string resourcePath = string `/merge`;
        map<any> headerValues = {"Authorization": self.apiKeyConfig.authorization};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        ApiResponseSuccess response = check self.clientEp->post(resourcePath, request, httpHeaders);
        return response;
    }
    # Convert raw HTML to PDF
    #
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function wkhtmltopdfFromHtmlPost(WkHtmlToPdfHtmlToPdfRequest payload) returns ApiResponseSuccess|error {
        string resourcePath = string `/wkhtmltopdf/html`;
        map<any> headerValues = {"Authorization": self.apiKeyConfig.authorization};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        ApiResponseSuccess response = check self.clientEp->post(resourcePath, request, httpHeaders);
        return response;
    }
    # Convert URL to PDF
    #
    # + url - Url of the page to convert to PDF. Must start with http:// or https://.
    # + output - Specify output=json to receive a JSON output. Defaults to PDF file.
    # + return - A PDF file or a JSON object depending on the `output` query parameter
    remote isolated function wkhtmltopdfFromUrlGET(string url, string? output = ()) returns ApiResponseSuccess|error {
        string resourcePath = string `/wkhtmltopdf/url`;
        map<anydata> queryParam = {"url": url, "output": output, "apikey": self.apiKeyConfig.apikey};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        ApiResponseSuccess response = check self.clientEp-> get(resourcePath);
        return response;
    }
    # Convert URL to PDF
    #
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function wkhtmltopdfFromUrlPost(WkHtmlToPdfUrlToPdfRequest payload) returns ApiResponseSuccess|error {
        string resourcePath = string `/wkhtmltopdf/url`;
        map<any> headerValues = {"Authorization": self.apiKeyConfig.authorization};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        ApiResponseSuccess response = check self.clientEp->post(resourcePath, request, httpHeaders);
        return response;
    }
    # Generate bar codes and QR codes with ZXING.
    #
    # + format - Most common is CODE_39 or QR_CODE
    # + value - Specify the text value you want to convert
    # + showlabel - Show label of text below barcode
    # + height - Height of the barcode generated image
    # + width - Width of the barcode generated image
    # + return - An image of the generated barcode or QR code
    remote isolated function zebraGET(string format, string value, boolean? showlabel = (), int? height = (), int? width = ()) returns string|error {
        string resourcePath = string `/zebra`;
        map<anydata> queryParam = {"format": format, "value": value, "showlabel": showlabel, "height": height, "width": width, "apikey": self.apiKeyConfig.apikey};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        string response = check self.clientEp-> get(resourcePath);
        return response;
    }
}
