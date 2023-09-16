import ballerina/http;
import ballerina/constraint;

@constraint:Int{
    maxValueExclusive: 100,
    minValue: 10
}
public type Position int;

public type Child record {
    Position position;
};

service /payloadV on new http:Listener(9090) {
    resource function post pet(@http:Payload Child body) returns error? {
        return;
    }
}