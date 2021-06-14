public type Countryrecord {
    string id?;
    string value?;
};

public type YouthLiteracyRaterecord {
    record {} indicator?;
    Country country?;
    string date?;
    int value?;
    int 'decimal?;
};

public type Errorrecord {
    string name?;
};
