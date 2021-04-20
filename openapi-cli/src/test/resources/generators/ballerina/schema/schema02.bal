public type Pet record {
    int id;
    string name;
    string tag?;
    string 'type?;
};

public type Error record {
    int code;
    string message;
};
