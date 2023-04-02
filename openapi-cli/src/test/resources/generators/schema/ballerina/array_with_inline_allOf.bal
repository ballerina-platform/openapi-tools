# List of addresses
public type Owner record {
    record {
        # Street No
        string streetNo?;
        # House number
        string houseNo?;
        # Street Name
        string streatName?;
        # Country Name
        string country?;
        # Zip code
        int zipCode?;
        *NameData;
    }[] AdressList?;
    string[] Pets?;
};

public type NameData record {
    string FirstName?;
    string LastName?;
};
