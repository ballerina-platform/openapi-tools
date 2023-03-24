public type Address record {
    # Street Number
    string streetNo?;
    # House Number
    string houseNo?;
}|record {
    # Street Name
    string streatName?;
    # Country Name
    string country?;
}|record {
    # Zipcode
    int zipCode?;
}|record {}|CountryDetails;

public type CountryDetails record {
    string iso_code?;
    string name?;
};
