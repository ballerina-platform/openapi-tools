public type User record {
    string first_name?;
    string last_name?;
};

public type Activity record {
    string uuid?;
};

public type Activities record {
    int offset?;
    Activity[] history?;

};

public type AnyOF User|Activity;