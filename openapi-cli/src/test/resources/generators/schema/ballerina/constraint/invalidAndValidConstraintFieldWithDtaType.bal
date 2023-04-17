import ballerina/constraint;

@constraint:String {maxLength: 5}
public type Address string;

public type Person record {
    string name?;
    @constraint:Int {maxValue: 5}
    int id;
};
