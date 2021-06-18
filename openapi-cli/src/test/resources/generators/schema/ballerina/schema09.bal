public type Dog record {
    *Pet;
    boolean bark?;
};

public type Pet record {
    int id;
    string name;
    string tag?;
    string 'type?;
};
