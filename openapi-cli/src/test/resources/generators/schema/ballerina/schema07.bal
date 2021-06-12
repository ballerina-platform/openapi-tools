
public type Pet record {
    int id;
    string name;
    string tag?;
    Tag 'type?;
};

public type Dog record {
    Pet[] pets?;
    boolean bark;
};

public type Tag record {
    int id?;
    string tagType?;
};
