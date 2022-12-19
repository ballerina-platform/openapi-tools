import ballerina/constraint;

public type Address record {
    int streetNo?;
    @constraint:Array {maxLength: 2}
    string[] mainStreet?;
    string country?;
};

@constraint:Array {maxLength: 7}
public type UserAddress int[];

public type Pet string[];
