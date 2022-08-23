import ballerina/constraint;

@constraint:String {maxLength: 5000}
public type TaxratesItemsString string;

@constraint:String {maxLength: 5000}
public type TaxratesanyofItemsString string;

public type TaxratesoneofarrayItemsNull string|int;

public type TaxratesanyofarrayItemsNull int|string;

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
    @constraint:Int {minValue: 1, maxValue: 100}
    int maxDeliveryCount?;
    # scenario 01 - field with nullable.
    string? service_class?;
    # scenario 02 - field with oneOf type.
    TaxratesItemsString[]|int tax_rates?;
    # scenario 03 - field with anyOf.
    TaxratesanyofItemsString[]|int tax_rates_anyOf?;
    # scenario 03 - field with a oneOf type array that items has oneOf.
    (string|int)[]|string tax_rates_oneOF_array?;
    # scenario 04 - field with a anyOf type array items has anyOf.
    (int|string)[]|int tax_rates_anyOf_array?;
};
