public type default_query record {
    string 'limit?;
    int id?;
};

public type InventoryItem record {
    string id;
    string name;
    string releaseDate;
    Manufacturer manufacturer;
};

public type default_nullable record {
    string 'limit?;
    int id?;
};

public type required_query record {
    string name?;
    int id?;
};

public type required_nullable record {
    string name?;
    int id?;
};

public type Manufacturer record {
    string name;
    string homePage?;
    string phone?;
};

public type optional_query record {
    string rank?;
    int id?;
};

public type optional_nullable record {
    string rank?;
    int id?;
};

public type PrimitiveValues 0|1;

public type add_false record {|
    string name?;
|};
