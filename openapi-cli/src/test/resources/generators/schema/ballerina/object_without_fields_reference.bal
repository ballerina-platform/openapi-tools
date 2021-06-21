public type YouthLiteracyRate record {
    record {} indicator?;
    Country country?;
    string date?;
    int value?;
    int 'decimal?;
};

public type Country record {
    string id?;
    string value?;
};

public type Error record {
    string name?;
};
