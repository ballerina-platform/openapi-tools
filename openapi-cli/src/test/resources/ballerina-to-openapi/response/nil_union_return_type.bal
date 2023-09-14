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
    # for `get` method with nil return type and no error status code
    resource function get path_get_error() returns error? {
    };

    # for `get` method with nil return type and no error status code
    resource function get path_get() returns string? {
    };

    # for `get` method with nil return type and no error status code
    resource function get path_get_query_param(string id) returns string? {
    };

    # for `post` method with 201 payload with optional error status code
    resource function post path_post() returns Links|http:NotFound? {
    };

    # for union return type with error status code
    resource function get path() returns string|http:NotFound? {
    };

    # with same error status code (202): by default for ? and explicitly mentioned as return type
    resource function post path(string id) returns http:Accepted|http:NotFound|BadRequestRecord? {
    };

    resource function get path_with_query(string id) returns string|http:NotFound? {
    };

    resource function post path_with_path/[string id]() returns http:NotFound? {
    };

    resource function post path_with_headers(@http:Header string header) returns http:NotFound? {
    };

    resource function post path_with_request_body(@http:Payload string payload) returns http:NotFound? {
    };
}
