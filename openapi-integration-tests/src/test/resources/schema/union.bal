import ballerina/constraint;

@constraint:String {maxLength: 5000}
public type TaxratesItemsString string;

@constraint:String {maxLength: 5000}
public type TaxratesanyofItemsString string;

public type TaxratesoneofarrayItemsNull string|int;

public type TaxratesanyofarrayItemsNull int|string;

public type Person record {
    # scenario 01 - field with nullable.
    string? service_class;
    # scenario 02 - field with oneOf type.
    TaxratesItemsString[]|int tax_rates?;
    # scenario 03 - field with anyOf.
    TaxratesanyofItemsString[]|int tax_rates_anyOf?;
    # scenario 03 - field with a oneOf type array that items has oneOf.
    (string|int)[]|string tax_rates_oneOF_array?;
    # scenario 04 - field with a anyOf type array items has anyOf.
    (int|string)[]|int tax_rates_anyOf_array?;
};
