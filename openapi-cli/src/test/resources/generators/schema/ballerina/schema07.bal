
public type Tag record {
    int id?;
    string tagType?;
};

public type Dog record {
    Pet[] pets?;
    boolean bark;
};

public type Pet record {
    int id;
    string name;
    string tag?;
    Tag 'type?;
};
