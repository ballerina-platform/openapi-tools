import ballerina/http;

public type AnydataJsonXmlBadRequest record {|
    *http:BadRequest;
    anydata|json|xml body;
|};

public type AnydataJsonXmlNotFound record {|
    *http:NotFound;
    anydata|json|xml body;
|};

public type JsonOk record {|
    *http:Ok;
    json body;
|};

public type StringInternalServerError record {|
    *http:InternalServerError;
    string body;
|};

public type AnydataBadRequest record {|
    *http:BadRequest;
    anydata body;
|};
