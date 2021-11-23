import ballerina/http;

# Provides API key configurations needed when communicating with a remote HTTP endpoint.
public type ApiKeysConfig record {|
    # Autherization header
    string authorization;
    # Api key query name
    string apikey;
|};

# Client endpoint for the given API
public isolated client class Client {
    final http:Client clientEp;
    final readonly & ApiKeysConfig apiKeyConfig;
    # Gets invoked to initialize the `connector`.
    #
    # + apiKeyConfig - API keys for authorization
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ApiKeysConfig apiKeyConfig, string serviceUrl, http:ClientConfiguration clientConfig =  {}) returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        self.apiKeyConfig = apiKeyConfig.cloneReadOnly();
        return;
    }
    # Convert raw HTML to PDF
    #
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function chromeFromHtmlPost(ChromeHtmlToPdfRequest payload) returns ApiResponseSuccess|error {
        string  path = string `/chrome/html`;
        map<any> headerValues = {"Authorization": self.apiKeyConfig.authorization};
        map<string|string[]> accHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody, "application/json");
        ApiResponseSuccess response = check self.clientEp->post(path, request, headers = accHeaders);
        return response;
    }
    # Convert URL to PDF
    #
    # + url - Url of the page to convert to PDF. Must start with http:// or https://.
    # + output - Specify output=json to receive a JSON output. Defaults to PDF file.
    # + return - A PDF file or a JSON object depending on the `output` query parameter
    remote isolated function chromeFromUrlGET(string url, string? output = ()) returns ApiResponseSuccess|error {
        string  path = string `/chrome/url`;
        map<anydata> queryParam = {"url": url, "output": output, "apikey": self.apiKeyConfig.apikey};
        path = path + check getPathForQueryParam(queryParam);
        ApiResponseSuccess response = check self.clientEp-> get(path);
        return response;
    }
    # Convert URL to PDF
    #
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function chromeFromUrlPost(ChromeUrlToPdfRequest payload) returns ApiResponseSuccess|error {
        string  path = string `/chrome/url`;
        map<any> headerValues = {"Authorization": self.apiKeyConfig.authorization};
        map<string|string[]> accHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody, "application/json");
        ApiResponseSuccess response = check self.clientEp->post(path, request, headers = accHeaders);
        return response;
    }
    # Convert office document or image to PDF
    #
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function libreConvertPost(LibreOfficeConvertRequest payload) returns ApiResponseSuccess|error {
        string  path = string `/libreoffice/convert`;
        map<any> headerValues = {"Authorization": self.apiKeyConfig.authorization};
        map<string|string[]> accHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody, "application/json");
        ApiResponseSuccess response = check self.clientEp->post(path, request, headers = accHeaders);
        return response;
    }
    # Merge multiple PDFs together
    #
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function mergePost(MergeRequest payload) returns ApiResponseSuccess|error {
        string  path = string `/merge`;
        map<any> headerValues = {"Authorization": self.apiKeyConfig.authorization};
        map<string|string[]> accHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody, "application/json");
        ApiResponseSuccess response = check self.clientEp->post(path, request, headers = accHeaders);
        return response;
    }
    # Convert raw HTML to PDF
    #
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function wkhtmltopdfFromHtmlPost(WkHtmlToPdfHtmlToPdfRequest payload) returns ApiResponseSuccess|error {
        string  path = string `/wkhtmltopdf/html`;
        map<any> headerValues = {"Authorization": self.apiKeyConfig.authorization};
        map<string|string[]> accHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody, "application/json");
        ApiResponseSuccess response = check self.clientEp->post(path, request, headers = accHeaders);
        return response;
    }
    # Convert URL to PDF
    #
    # + url - Url of the page to convert to PDF. Must start with http:// or https://.
    # + output - Specify output=json to receive a JSON output. Defaults to PDF file.
    # + return - A PDF file or a JSON object depending on the `output` query parameter
    remote isolated function wkhtmltopdfFromUrlGET(string url, string? output = ()) returns ApiResponseSuccess|error {
        string  path = string `/wkhtmltopdf/url`;
        map<anydata> queryParam = {"url": url, "output": output, "apikey": self.apiKeyConfig.apikey};
        path = path + check getPathForQueryParam(queryParam);
        ApiResponseSuccess response = check self.clientEp-> get(path);
        return response;
    }
    # Convert URL to PDF
    #
    # + payload - A JSON object as a payload is required within the body of the request. The following attributes of the JSON object are detailed below:
    # + return - A JSON object containing the url to the PDF and other meta data
    remote isolated function wkhtmltopdfFromUrlPost(WkHtmlToPdfUrlToPdfRequest payload) returns ApiResponseSuccess|error {
        string  path = string `/wkhtmltopdf/url`;
        map<any> headerValues = {"Authorization": self.apiKeyConfig.authorization};
        map<string|string[]> accHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody, "application/json");
        ApiResponseSuccess response = check self.clientEp->post(path, request, headers = accHeaders);
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
        string  path = string `/zebra`;
        map<anydata> queryParam = {"format": format, "value": value, "showlabel": showlabel, "height": height, "width": width, "apikey": self.apiKeyConfig.apikey};
        path = path + check getPathForQueryParam(queryParam);
        string response = check self.clientEp-> get(path);
        return response;
    }
}
