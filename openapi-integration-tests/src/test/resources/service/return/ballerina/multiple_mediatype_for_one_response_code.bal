import ballerina/http;

public type User record {
    string userName;
    string firstName?;
    string lastName?;
};

public type BadRequestUserXmlString record {|
    *http:BadRequest;
    User|xml|string body;
    map<string|string[]> headers;
|};
