import ballerina/constraint;

@constraint:String {pattern: re `[ a-zA-Z0-9/.+!@#$%^&*()+\- ]*`}
public type PersonHobbyItemsString string;

public type Person record {
    @constraint:String {maxLength: 14}
    string name?;
    PersonHobbyItemsString[] hobby?;
    @constraint:Int {maxValue: 5}
    int id;
    @constraint:String {pattern: re `^[a-zA-Z0-9_]+$`}
    string salary?;
    string net?;
    int count?;
};
