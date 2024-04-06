import ballerina/http;

public type Category record {
    string name?;
};

public type OkPet record {|
    *http:Ok;
    Pet body;
    map<string|string[]> headers;
|};

public type Pet record {
    int id?;
    string? name;
    # pet status in the store
    "available"|"pending"|"sold"? status?;
};
