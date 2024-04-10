import ballerina/http;

public type OkAnydata record {|
    *http:Ok;
    anydata body;
    map<string|string[]> headers;
|};
