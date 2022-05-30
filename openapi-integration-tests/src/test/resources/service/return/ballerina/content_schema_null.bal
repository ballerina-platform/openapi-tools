import ballerina/http;

public type AcceptedJson record {|
    *http:Accepted;
    json body;
|};
