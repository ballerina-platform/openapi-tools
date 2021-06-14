public type Pet string[][];
public type Address record {
    int streetNo?;
    string mainStreet?;
    string country?;
    };

public type UserAddress Address[][][];