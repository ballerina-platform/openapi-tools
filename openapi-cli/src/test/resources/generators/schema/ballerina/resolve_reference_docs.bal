public type Pets record {
    Pet[] pet_details?;
    int numer_of_pets?;
};

public type Dog record {
    # Pet details
    Pet pet_details?;
    boolean bark?;
};

# Pet details
public type Pet record {
    int id;
    string name;
    string tag?;
    string 'type?;
};
