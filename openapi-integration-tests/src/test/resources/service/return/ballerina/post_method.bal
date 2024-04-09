import ballerina/http;

public type OkString record {|
    *http:Ok;
    string body;
    map<string|string[]> headers;
|};
