import ballerina/http;

public type InternalServerErrorString record {|
    *http:InternalServerError;
    string body;
|};

public type OkJson record {|
    *http:Ok;
    json body;
|};

public type NotFoundAnydata record {|
    *http:NotFound;
    anydata body;
|};

public type BadRequestAnydata record {|
    *http:BadRequest;
    anydata body;
|};
