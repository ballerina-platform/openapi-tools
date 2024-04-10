import ballerina/http;

public type BadRequestInline_response_400 record {|
    *http:BadRequest;
    Inline_response_400 body;
    map<string|string[]> headers;
|};

public type Inline_response_400 record {
    # The error ID.
    int id?;
    # The error name.
    string errorType?;
};
