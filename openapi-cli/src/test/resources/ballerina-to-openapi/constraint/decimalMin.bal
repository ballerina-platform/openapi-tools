import ballerina/http;
import ballerina/constraint;

@constraint:Number {
    minValueExclusive: 2.55,
    maxValue: 5.55
}

public type Marks decimal;

public type School record {
    Marks marks;
};

service /payloadV on new http:Listener(9090) {
    resource function post pet(@http:Payload School body) returns error? {
        return;
    }
}