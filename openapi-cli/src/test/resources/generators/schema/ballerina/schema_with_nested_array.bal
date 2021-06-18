public type Address record {
    int streetNo?;
    string mainStreet?;
    string country?;
};

public type UserAddress Address[][][];

public type Pet string[][];
