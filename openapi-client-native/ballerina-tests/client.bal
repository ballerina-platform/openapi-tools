import ballerina/http;
import ballerina/jballerina.java;

function setModule() = @java:Method {'class: "io.ballerina.openapi.client.ModuleUtils"} external;

function init() {
    setModule();
}

type ClientMethodImpl record {|
    string name;
|};

annotation ClientMethodImpl MethodImpl on function;

type ClientMethodInvocationError http:ClientError;

public isolated client class Client {
    final http:StatusCodeClient clientEp;

    public isolated function init(string serviceUrl = "http://localhost:9999/api") returns error? {
        http:StatusCodeClient httpEp = check new (serviceUrl);
        self.clientEp = httpEp;
        return;
    }

    @MethodImpl {name: "getAlbumsIdImpl"}
    resource isolated function get albums/[string id](map<string|string[]> headers = {}, typedesc<OkAlbum|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    private isolated function getAlbumsIdImpl(string id, map<string|string[]> headers, typedesc<OkAlbum|NotFoundErrorMessage|BadRequestErrorPayload> targetType) returns OkAlbum|NotFoundErrorMessage|BadRequestErrorPayload|error {
        string resourcePath = string `/albums/${getEncodedUri(id)}`;
        return self.clientEp->get(resourcePath, headers, targetType = targetType);
    }

    @MethodImpl {name: "getAlbumsImpl"}
    resource isolated function get albums(map<string|string[]> headers = {}, typedesc<OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>, *GetAlbumsQueries queries) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    private isolated function getAlbumsImpl(map<string|string[]> headers, typedesc<OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType, *GetAlbumsQueries queries) returns OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload|error {
        string resourcePath = string `/albums`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        return self.clientEp->get(resourcePath, headers, targetType = targetType);
    }

    @MethodImpl {name: "getAlbumsAllImpl"}
    resource isolated function get albumsAll(map<string|string[]> headers = {}, typedesc<OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    @MethodImpl {name: "getAlbumsAllImpl"}
    remote isolated function getAlbumsAll(map<string|string[]> headers = {}, typedesc<OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    private isolated function getAlbumsAllImpl(map<string|string[]> headers, typedesc<OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType) returns OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload|error {
        string resourcePath = string `/albums-all`;
        return self.clientEp->get(resourcePath, headers, targetType = targetType);
    }

    @MethodImpl {name: "postAlbumsImpl"}
    resource isolated function post albums(http:Request req, map<string|string[]> headers = {}, typedesc<CreatedAlbum|ConflictAlbum|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    @MethodImpl {name: "postAlbumsImpl"}
    remote isolated function postAlbums(http:Request req, map<string|string[]> headers = {}, typedesc<CreatedAlbum|ConflictAlbum|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    private isolated function postAlbumsImpl(http:Request req, map<string|string[]> headers, typedesc<CreatedAlbum|ConflictAlbum|BadRequestErrorPayload> targetType) returns CreatedAlbum|ConflictAlbum|BadRequestErrorPayload|error {
        string resourcePath = string `/albums`;
        return self.clientEp->post(resourcePath, req, headers, targetType = targetType);
    }

    @MethodImpl {name: "postAlbums1Impl"}
    resource isolated function post albums1/[string a]/[int b](http:Request req, map<string|string[]> headers = {}, typedesc<CreatedAlbum|ConflictAlbum|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    @MethodImpl {name: "postAlbums1Impl"}
    remote isolated function postAlbums1(string a, int b, http:Request req, map<string|string[]> headers = {}, typedesc<CreatedAlbum|ConflictAlbum|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    private isolated function postAlbums1Impl(string a, int b, http:Request req, map<string|string[]> headers, typedesc<CreatedAlbum|ConflictAlbum|BadRequestErrorPayload> targetType) returns CreatedAlbum|ConflictAlbum|BadRequestErrorPayload|error {
        string resourcePath = string `/albums`;
        return self.clientEp->post(resourcePath, req, headers, targetType = targetType);
    }

    @MethodImpl {name: "postAlbumsAllImpl"}
    resource isolated function post albumsAll/[string a](Album[] albums, map<string|string[]> headers = {}, typedesc<CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType = <>, *PostAlbumsAllQueries queries) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    @MethodImpl {name: "postAlbumsAllImpl"}
    remote isolated function postAlbumsAll(string a, Album[] albums, map<string|string[]> headers = {}, typedesc<CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType = <>, *PostAlbumsAllQueries queries) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    private isolated function postAlbumsAllImpl(string a, Album[] albums, map<string|string[]> headers, typedesc<CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType, *PostAlbumsAllQueries queries) returns CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload|error {
        string resourcePath = string `/albums-all`;
        return self.clientEp->post(resourcePath, albums, headers, targetType = targetType);
    }

    @MethodImpl {name: "postAlbumsAll1Impl"}
    resource isolated function post albumsAll1(http:Request req, map<string|string[]> headers = {}, typedesc<CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType = <>, *PostAlbumsAllQueries queries) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    @MethodImpl {name: "postAlbumsAll1Impl"}
    remote isolated function postAlbumsAll1(http:Request req, map<string|string[]> headers = {}, typedesc<CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType = <>, *PostAlbumsAllQueries queries) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    private isolated function postAlbumsAll1Impl(http:Request req, map<string|string[]> headers, typedesc<CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType, *PostAlbumsAllQueries queries) returns CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload|error {
        string resourcePath = string `/albums-all`;
        return self.clientEp->post(resourcePath, req, headers, targetType = targetType);
    }

    @MethodImpl {name: "postAlbumsAll2Impl"}
    resource isolated function post albumsAll2(Album[] albums, http:Request req, map<string|string[]> headers = {}, typedesc<CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType = <>, *PostAlbumsAllQueries queries) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    @MethodImpl {name: "postAlbumsAll2Impl"}
    remote isolated function postAlbumsAll2(Album[] albums, http:Request req, map<string|string[]> headers = {}, typedesc<CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType = <>, *PostAlbumsAllQueries queries) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    private isolated function postAlbumsAll2Impl(Album[] albums, http:Request req, map<string|string[]> headers, typedesc<CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType, *PostAlbumsAllQueries queries) returns CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload|error {
        string resourcePath = string `/albums-all`;
        return self.clientEp->post(resourcePath, albums, headers, targetType = targetType);
    }

    @MethodImpl {name: "getAlbumsIdImpl"}
    remote isolated function getAlbumsId(string id, map<string|string[]> headers = {}, typedesc<OkAlbum|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    @MethodImpl {name: "getAlbumsImpl"}
    remote isolated function getAlbums(map<string|string[]> headers = {}, typedesc<OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>, *GetAlbumsQueries queries) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    @MethodImpl {name: "getAlbumsImpl1"}
    remote isolated function getAlbums1(map<string|string[]> headers = {}, typedesc<OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>, *GetAlbumsQueries queries) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    remote isolated function getAlbums2(map<string|string[]> headers = {}, typedesc<OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>, *GetAlbumsQueries queries) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    @MethodImpl {name: "getAlbumsImpl3"}
    remote isolated function getAlbums3(map<string|string[]> headers = {}, typedesc<OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>, *GetAlbumsQueries queries) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    private isolated function getAlbumsImpl3(map<string|string[]> headers, *GetAlbumsQueries queries) returns OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload|error {
        return {
            body: [],
            headers: {
                req\-id: 123,
                user\-id: "456"
            }
        };
    }
}
