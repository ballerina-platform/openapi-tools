import ballerina/http;
import ballerina/constraint;

@constraint:String {maxLength: 23}
public type HobbyItemsString string;

@constraint:String {minLength: 7}
public type PersonDetailsItemsString string;

@constraint:String {minLength: 5}
public type SchoolName string;

@constraint:Float {maxValue: 445.4}
public type PersonFeeItemsNumber float;

@constraint:Int {maxValue: 67 }
public type PersonLimitItemsInteger int;

@constraint:Array {maxLength: 5, minLength: 2}
public type Hobby HobbyItemsString[];

@constraint:Array {length: 15}
public type School SchoolName[];

public type Person record {
    Hobby hobby?;
    School school?;
    @constraint:Array {maxLength: 5}
    PersonDetailsItemsString[] Details?;
    int id;
    PersonFeeItemsNumber[] fee?;
    # The maximum number of items in the response (as set in the query or by default).
    PersonLimitItemsInteger[] 'limit?;
};

service /payloadV on new http:Listener(9090) {
    resource function post pet(@http:Payload Person body) returns error? {
        return;
    }
}
