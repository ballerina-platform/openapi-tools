import ballerina/http;
# Represents a product
#
# + name - Name of the product
# + description - Product description
# + price - Product price
public type Product record {|
    string id?;
    string name;
    string description;
    Price price;
|};

public enum Currency {
    USD,
    LKR,
    SGD,
    GBP
}

public type Price record {|
    Currency currency;
    float amount;
|};

public type Error record {|
    string code;
    string message;
|};

public type ErrorResponse record {|
    Error 'error;
|};

public type BadRequest record {|
    *http:BadRequest;

    ErrorResponse body;
|};

public const string TEXT_HTML = "text/html";
