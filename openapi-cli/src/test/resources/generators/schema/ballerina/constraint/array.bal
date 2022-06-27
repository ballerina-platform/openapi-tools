import ballerina/constraint;

@constraint:String {maxLength: 23}
public type HobbyItemsString string;

@constraint:Array {maxLength: 5, minLength: 2}
public type Hobby HobbyItemsString[];

@constraint:String {minLength: 7}
public type PersonDetailsItemsString string;

@constraint:Float {maximum: 445.4}
public type PersonFeeItemsNumber float;

public type Person record {
    Hobby hobby?;
    @constraint:Array {maxLength: 5}
    PersonDetailsItemsString[] salaryDetails?;
    int id;
    PersonFeeItemsNumber[] fee?;
};
