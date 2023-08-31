public type User record {
    int id?;
    string username?;
    string email?;
    string name?;
    Address address?;
};

public type Address record {
    string street?;
    string city?;
};

public type Id Name;

public type Name string;