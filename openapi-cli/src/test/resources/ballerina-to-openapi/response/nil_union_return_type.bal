import ballerina/http;

type Links record {|
    int id;
    string[] links;
|};

 public type BadRequestRecord record {|
    *http:BadRequest;
    string body;
 |};


service /payloadV on new http:Listener(9090) {
    # for `get` method with nil return type and error
    resource function get path_get_error() returns error? {
    };

    # for `get` method with nil return type and success status code
    resource function get path_get() returns string? {
    };

    # for `get` method with nil return type and success status code with query param
    resource function get path_get_query_param(string id) returns string? {
    };

    # for `post` method with 201 payload with error status code and nil type
    resource function post path_post() returns Links|http:NotFound? {
    };

    # for union return type with error status code and nil type
    resource function get path() returns string|http:NotFound? {
    };

    # resource has 3 status codes that explicitly returns (202, 404, 400) while ? type returns implicitly 400, 202
    resource function post path(string id) returns http:Accepted|http:NotFound|BadRequestRecord? {
    };

    # resource has 4 status codes that explicitly returns (200, 404) while ? type returns implicitly 400, 202
    resource function get path_with_query(string id) returns string|http:NotFound? {
    };

    # resource has 3 status codes that explicitly returns (404) while ? type returns implicitly 400, 202
    resource function post path_with_path/[string id]() returns http:NotFound? {
    };

    # method has 3 status codes that explicitly returns (404) while ? type returns implicitly 400, 202
    resource function post path_with_headers(@http:Header string header) returns http:NotFound? {
    };

    resource function post path_with_request_body(@http:Payload string payload) returns http:NotFound? {
    };
}
