public type CreatedPet record {
    string petId?;
    string createdDate?;
};

public type Pets Pet[];

public type Error record {
    int code;
    string message;
};

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
