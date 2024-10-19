import ballerina/http;

public type StringOk record {|
    *http:Ok;
    string body;
|};
