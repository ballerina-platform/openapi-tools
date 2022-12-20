import ballerina/http;

public type BadRequestUserStringXml record {|
    *http:BadRequest;
    User|string|xml body;
|};

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

public type Pet record {
    string userName;
    string firstName?;
    string lastName?;
};
