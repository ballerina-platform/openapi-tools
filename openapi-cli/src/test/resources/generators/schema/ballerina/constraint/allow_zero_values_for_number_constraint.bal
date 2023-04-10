import ballerina/constraint;

public type Address string;

public type Person record {
    string name?;
    string[] hobby?;
    @constraint:Int {maxValue: 0}
    int id;
    Address address?;
    @constraint:Float {maxValue: 0}
    float salary?;
    @constraint:Number {minValue: 0}
    decimal net?;
};
