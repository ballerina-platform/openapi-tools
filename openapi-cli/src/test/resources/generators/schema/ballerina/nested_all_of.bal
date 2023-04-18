# Make this object schema name with simple case because the tool will convert it to camel case.
public type Account record {
    # Username of the account
    string username?;
    string nickname?;
    # The status of the account. Currently the only possible value is "active", but more values may be added in the future
    string account_status?;
    # Display name of the account
    string display_name?;
    # Website of the account
    string website?;
    # The created date of the account
    string created_on?;
    # The UUID of the account
    string uuid?;
    # Indicates whether two factor authentication is on
    boolean has_2fa_enabled?;
};

# Nested allOf with reference
public type User record {
    *Account;
    # Indicates whether the user represents staff
    boolean is_staff?;
    # The user's Atlassian account ID
    string account_id?;
};

public type Address record {
    string streetNo?;
    string houseNo?;
    string streatName?;
    string country?;
    int zipCode?;
};
