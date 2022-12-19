public type PrimaryEducationExpenditure record {
    Indicator indicator?;
    Country country?;
    string date?;
    int value?;
    int 'decimal?;
};

public type YouthLiteracyRate record {
    Indicator indicator?;
    Country country?;
    string date?;
    int value?;
    int 'decimal?;
};

public type AccessToElectricity record {
    Indicator indicator?;
    Country country?;
    string date?;
    int value?;
    int 'decimal?;
};

public type GrossDomesticProduct record {
    Indicator indicator?;
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

public type CountryPolutation record {
    Indicator indicator?;
    Country country?;
    string date?;
    int value?;
    int 'decimal?;
};

public type Indicator record {
    string id?;
    string value?;
};
