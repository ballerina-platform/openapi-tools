public type Address record{string streetNo?;string houseNo?;}|record{string streatName?;string country?;}|record{int zipCode?;}|record{}|CountryDetails;

public type CountryDetails record {
    string iso_code?;
    string name?;
};