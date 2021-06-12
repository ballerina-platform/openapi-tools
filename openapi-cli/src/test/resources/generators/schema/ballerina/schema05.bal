# Pet details
public type Pet record {
    int id;
    string name;
    string tag?;
};

public type Dog record {
    # Pet details
    Pet pets?;
    boolean bark;
};
