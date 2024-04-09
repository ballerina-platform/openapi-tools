 import ballerina/http;

public type AcceptedString record {|
    *http:Accepted;
    string body;
    map<string|string[]> headers;
|};
