public type Address record {
    int streetNo?;
    anydata[] street?;
    string country?;
};

public type UserAddress anydata[];

public type Pet string[];
