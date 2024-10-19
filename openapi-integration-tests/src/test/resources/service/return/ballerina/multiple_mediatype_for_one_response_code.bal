import ballerina/http;

public type User record {
    string userName;
    string firstName?;
    string lastName?;
};

public type UserXmlStringBadRequest record {|
    *http:BadRequest;
    User|xml|string body;
|};
