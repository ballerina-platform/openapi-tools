import ballerina/http;

public type ErrorPayload record {
    string timestamp;
    int status;
    string reason;
    string message;
    string path;
    string method;
};

public type BadRequestErrorPayload record {|
    *http:BadRequest;
    ErrorPayload body;
    map<string|string[]> headers;
|};

public type OkAlbumArray record {|
    *http:Ok;
    Album[] body;
    record {|int req\-id; string user\-id;|} headers;
|};

public type Album record {|
    readonly string id;
    string name;
    string artist;
    string genre;
|};

public type OkAlbum record {|
    *http:Ok;
    Album body;
    record {|int req\-id; string user\-id;|} headers;
|};

public type CreatedAlbum record {|
    *http:Created;
    Album body;
    record {|int req\-id; string user\-id;|} headers;
|};

public type CreatedAlbumArray record {|
    *http:Created;
    Album[] body;
    record {|int req\-id; string user\-id;|} headers;
|};

public type ConflictAlbum record {|
    *http:Conflict;
    ErrorMessage body;
    record {|int req\-id; string user\-id;|} headers;
|};

public type ErrorMessage record {|
    string message;
    string...;
|};

public type NotFoundErrorMessage record {|
    *http:NotFound;
    ErrorMessage body;
    record {|int req\-id; string user\-id;|} headers;
|};
