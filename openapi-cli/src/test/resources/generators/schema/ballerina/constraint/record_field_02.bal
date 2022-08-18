import ballerina/constraint;

public type Address string;

public type Person record {
    @constraint:String {maxLength: 14}
    string name?;
    @constraint:Array {maxLength: 5}
    string[] hobby?;
    @constraint:Int {maxValue: 5}
    int id;
    Address address?;
    float salary?;
    decimal net?;
};
