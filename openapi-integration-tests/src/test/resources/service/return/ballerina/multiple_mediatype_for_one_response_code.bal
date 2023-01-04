import ballerina/http;

public type BadRequestUserXmlString record {|
    *http:BadRequest;
    User|xml|string body;
|};

public type User record {|
    string userName;
    string firstName?;
    string lastName?;
|};

public type PetForm record {|
    string userName;
    string firstName?;
    string lastName?;
|};

public type Pet record {|
    string userName;
    string firstName?;
    string lastName?;
|};
