import ballerina/http;
import ballerina/constraint;

@constraint:Float {
    minValueExclusive: 2.5,
    maxValue: 10.5
}

public type Rating float;

public type Hotel record {
    Rating rate;
};

service /payloadV on new http:Listener(9090) {
    resource function post pet(@http:Payload Hotel body) returns error? {
        return;
    }
}