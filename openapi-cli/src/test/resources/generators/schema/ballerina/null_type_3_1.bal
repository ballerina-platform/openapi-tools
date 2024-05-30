import ballerina/http;

public type Category record {
    string name?;
};

public type PetOk record {|
    *http:Ok;
    Pet body;
|};

public type Pet record {
    int id?;
    string? name;
    # pet status in the store
    "available"|"pending"|"sold"? status?;
};
