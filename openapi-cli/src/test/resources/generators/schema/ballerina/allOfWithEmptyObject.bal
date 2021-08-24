public type Pets Pet[];

public type Error record {
    int code;
    string message;
};

public type Pet record {
    string[] entries?;
};
