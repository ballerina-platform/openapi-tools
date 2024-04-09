import ballerina/http;

public type OkJson record {|
    *http:Ok;
    json body;
    map<string|string[]> headers;
|};

public type NotFoundAnydataJsonXml record {|
    *http:NotFound;
    anydata|json|xml body;
    map<string|string[]> headers;
|};

public type InternalServerErrorString record {|
    *http:InternalServerError;
    string body;
    map<string|string[]> headers;
|};

public type BadRequestAnydataJsonXml record {|
    *http:BadRequest;
    anydata|json|xml body;
    map<string|string[]> headers;
|};

public type BadRequestAnydata record {|
    *http:BadRequest;
    anydata body;
    map<string|string[]> headers;
|};
