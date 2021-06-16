public type Dog record {
    # Pet details
    Pet pets?;
    boolean bark;
};

# Pet details
public type Pet record {
    int id;
    string name;
    string tag?;
};
