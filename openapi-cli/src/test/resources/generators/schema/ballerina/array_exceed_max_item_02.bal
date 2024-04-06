import ballerina/constraint;

public type Pet string[];

public type Stock record {
    @constraint:Array {maxLength: 2147483637}
    int[] count?;
    string country?;
};
