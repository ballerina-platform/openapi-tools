import ballerina/constraint;

@constraint:String {pattern: re `[ a-zA-Z0-9/.+!@#$%^&*()+\- ]*`}
public type PersonHobbyItemsString string;

public type Person record {
    @constraint:String {maxLength: 14, pattern: re `^[a-zA-Z0-9]*$`}
    string name?;
    PersonHobbyItemsString[] hobby?;
    @constraint:Int {maxValue: 5}
    int id;
    float salary?;
    decimal net?;
    int count?;
};
