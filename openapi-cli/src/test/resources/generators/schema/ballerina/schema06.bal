public type Pet record {
    int id;
    string name;
    string tag?;
};

public type Dog record {
    # Pet array
    Pet[] pets?;
    boolean bark;
};
