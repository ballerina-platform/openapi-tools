import ballerina/constraint;

public type PersonHobbyItemsInteger int;

public type Person record {
    int name?;
    PersonHobbyItemsInteger[] hobby?;
    @constraint:Int {maxValue: 5}
    int id;
    decimal salary?;
};
