public type Profile01 record {
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

public type Subject record {
    int id?;
    string name?;
    Activity subject_type?;
};
