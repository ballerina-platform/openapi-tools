public type InventoryItem record {
    string id;
    string name;
    string releaseDate;
    Manufacturer manufacturer;
};

public type Default_query record {
    string 'limit?;
    int id?;
};

public type Optional_nullable record {
    string rank?;
    int id?;
};

public type Optional_query record {
    string rank?;
    int id?;
};

public type Manufacturer record {
    string name;
    string homePage?;
    string phone?;
};

public type Add_false record {|
    string name?;
|};

public type Required_nullable record {
    string name?;
    int id?;
};

public type Required_query record {
    string name?;
    int id?;
};

public type Default_nullable record {
    string 'limit?;
    int id?;
};

public type PrimitiveValues 0|1;
