import ballerina/http;

public type JsonAccepted record {|
    *http:Accepted;
    json body;
|};
