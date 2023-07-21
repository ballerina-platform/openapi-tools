import ballerina/http;

public type ReturnValueXML xml;

public type ReturnValueCustomRec record {|
    *http:BadGateway;
    ReturnValueXML body;
|};

service /payloadV on new http:Listener(9090) {

    resource function get challenges/[string challenged]() returns byte[]|error {
        return error("");
    }
}
