public type Pets Pet[];

public type Error record {
    int:Signed32 code;
    string message;
};

public type Pet record {
    string[] entries?;
};
