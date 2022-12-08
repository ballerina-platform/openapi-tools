import ballerina/http;

public type BadRequestInline_response_400 record {|
    *http:BadRequest;
    Inline_response_400 body;
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

public type Inline_response_400 record {|
    # The error ID.
    int id?;
    # The error name.
    string errorType?;
|};

public type Pet record {|
    string userName;
    string firstName?;
    string lastName?;
|};
