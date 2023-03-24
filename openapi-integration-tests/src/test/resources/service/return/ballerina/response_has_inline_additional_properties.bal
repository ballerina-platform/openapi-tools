public type Response record {|
    Inline_response_map200...;
|};

public type StoreInventory03Response record {|
    record {|record {string name?; string place?;}...;|}...;
|};

public type BadRequestStoreInventory05Response record {|
    *http:BadRequest;
    StoreInventory05Response body;
|};

public type StoreInventory05Response record {|
    User...;
|};

public type StoreInventory04Response record {|
    User...;
|};

public type User record {
    string name?;
    int id?;
};

public type Inline_response_map200 record {
    int id?;
    int age?;
};

public type Inline_response_200 record {|
    string name?;
    int age?;
    int...;
|};
