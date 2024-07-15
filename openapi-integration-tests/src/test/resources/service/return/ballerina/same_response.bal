import ballerina/http;

public type StringAccepted record {|
    *http:Accepted;
    string body;
|};
