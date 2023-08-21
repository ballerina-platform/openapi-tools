import ballerina/constraint;

public type Person record {
    @constraint:Number {minValueExclusive: 10}
    decimal net?;
    @constraint:Int {maxValueExclusive: 100}
    int maxDeliveryCount?;
    @constraint:Int {minValue: 1, maxValue: 99}
    int service_count?;
};