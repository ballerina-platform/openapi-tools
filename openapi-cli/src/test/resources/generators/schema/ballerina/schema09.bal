
public type Pet record {
    int id;
    string name;
    string tag?;
    string 'type?;
};

public type Dog record {
    *Pet;
    boolean bark?;
};
