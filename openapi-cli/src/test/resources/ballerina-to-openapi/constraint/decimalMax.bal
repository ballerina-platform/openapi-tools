import ballerina/http;
import ballerina/constraint;

@constraint:Number {
    maxValueExclusive: 5.55,
    minValue: 2.55
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