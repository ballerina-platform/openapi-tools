public type User record {
    string userName;
    string firstName?;
    string lastName?;
};

public type PetForm record {
    string userName;
    string firstName?;
    string lastName?;
};

public type InlineResponse200 User|Pet|PetForm;

public type Pet record {
    string userName;
    string firstName?;
    string lastName?;
};
