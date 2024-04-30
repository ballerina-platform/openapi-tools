import ballerina/constraint;

@constraint:String {pattern: re `[ a-zA-Z0-9/.+!@#$%^&*()+\- ]*`}
public type PersonHobbyItemsString string;

# Represents the Queries record for the operation: getGreeting
public type GetGreetingQueries record {
    # the input string name
    string name;
};

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
