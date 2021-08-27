public type Pets Pet[];

public type Error record {
    int code;
    string message;
};

public type Pet record {
    int id?;
    string name?;
    string tag?;
    string 'type?;
    string[] entries?;
};
