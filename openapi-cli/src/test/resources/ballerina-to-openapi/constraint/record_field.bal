import ballerina/http;
import ballerina/constraint;

@constraint:String {minLength: 5}
public type Address string;

public type Person record {
    @constraint:String {maxLength: 14}
    string name?;
    @constraint:Array {maxLength: 5, minLength: 2}
    string[] hobby?;
    @constraint:Int {maxValue: 5}
    int id;
    Address address?;
    @constraint:Float {maxValue: 100000}
    float salary?;
    @constraint:Number {minValue: 500000}
    decimal net?;
};

service /payloadV on new http:Listener(9090) {
    resource function post pet(@http:Payload Person body) returns error? {
        return;
    }
}