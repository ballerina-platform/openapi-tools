import ballerina/http;

public type BadRequestUserXmlString record {|
    *http:BadRequest;
    User|xml|string body;
|};

public type User record {
    string userName;
    string firstName?;
    string lastName?;
};
