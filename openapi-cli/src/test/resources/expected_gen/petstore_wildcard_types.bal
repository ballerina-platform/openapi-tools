import ballerina/http;

public type AnydataOk record {|
    *http:Ok;
    anydata body;
|};
