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
    final http:Client clientEp;

    public isolated function init(string serviceUrl = "http://localhost:9999/api") returns error? {
        http:Client httpEp = check new (serviceUrl);
        self.clientEp = httpEp;
        return;
    }

    @MethodImpl {name: "getAlbumsIdImpl"}
    resource isolated function get albums/[string id](typedesc<Album|OkAlbum|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    private isolated function getAlbumsIdImpl(string id, typedesc<Album|OkAlbum|NotFoundErrorMessage|BadRequestErrorPayload> targetType) returns Album|OkAlbum|NotFoundErrorMessage|BadRequestErrorPayload|error {
        string resourcePath = string `/albums/${getEncodedUri(id)}`;
        return self.clientEp->get(resourcePath, targetType = targetType);
    }

    @MethodImpl {name: "getAlbumsImpl"}
    resource isolated function get albums(string genre = "Rock", typedesc<Album[]|OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    private isolated function getAlbumsImpl(string genre, typedesc<Album[]|OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType) returns Album[]|OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload|error {
        string resourcePath = string `/albums`;
        map<anydata> queryParam = {"genre": genre};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp->get(resourcePath, targetType = targetType);
    }

    @MethodImpl {name: "getAlbumsAllImpl"}
    resource isolated function get albums\-all(typedesc<Album[]|OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    @MethodImpl {name: "getAlbumsAllImpl"}
    remote isolated function getAlbumsAll(typedesc<Album[]|OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    private isolated function getAlbumsAllImpl(typedesc<Album[]|OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType) returns Album[]|OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload|error {
        string resourcePath = string `/albums-all`;
        return self.clientEp->get(resourcePath, targetType = targetType);
    }

    @MethodImpl {name: "postAlbumsImpl"}
    resource isolated function post albums(http:Request req, typedesc<Album|CreatedAlbum|ConflictAlbum|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    @MethodImpl {name: "postAlbumsImpl"}
    remote isolated function postAlbums(http:Request req, typedesc<Album|CreatedAlbum|ConflictAlbum|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    private isolated function postAlbumsImpl(http:Request req, typedesc<Album|CreatedAlbum|ConflictAlbum|BadRequestErrorPayload> targetType) returns Album|CreatedAlbum|ConflictAlbum|BadRequestErrorPayload|error {
        string resourcePath = string `/albums`;
        return self.clientEp->post(resourcePath, req, targetType = targetType);
    }

    @MethodImpl {name: "postAlbums1Impl"}
    resource isolated function post albums\-1/[string a]/[int b](http:Request req, typedesc<Album|CreatedAlbum|ConflictAlbum|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    @MethodImpl {name: "postAlbums1Impl"}
    remote isolated function postAlbums1(string a, int b, http:Request req, typedesc<Album|CreatedAlbum|ConflictAlbum|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    private isolated function postAlbums1Impl(string a, int b, http:Request req, typedesc<Album|CreatedAlbum|ConflictAlbum|BadRequestErrorPayload> targetType) returns Album|CreatedAlbum|ConflictAlbum|BadRequestErrorPayload|error {
        string resourcePath = string `/albums`;
        return self.clientEp->post(resourcePath, req, targetType = targetType);
    }

    @MethodImpl {name: "postAlbumsAllImpl"}
    resource isolated function post albums\-all/[string a](Album[] albums, string? query = (), typedesc<Album|CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    @MethodImpl {name: "postAlbumsAllImpl"}
    remote isolated function postAlbumsAll(string a, Album[] albums, string? query = (), typedesc<Album|CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    private isolated function postAlbumsAllImpl(string a, Album[] albums, string? query, typedesc<Album|CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType) returns Album|CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload|error {
        string resourcePath = string `/albums-all`;
        return self.clientEp->post(resourcePath, albums, targetType = targetType);
    }

    @MethodImpl {name: "postAlbumsAll1Impl"}
    resource isolated function post albums\-all\-1(http:Request req, string? query = (), typedesc<Album|CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    @MethodImpl {name: "postAlbumsAll1Impl"}
    remote isolated function postAlbumsAll1(http:Request req, string? query = (), typedesc<Album|CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    private isolated function postAlbumsAll1Impl(http:Request req, string? query, typedesc<Album|CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType) returns Album|CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload|error {
        string resourcePath = string `/albums-all`;
        return self.clientEp->post(resourcePath, req, targetType = targetType);
    }

    @MethodImpl {name: "postAlbumsAll2Impl"}
    resource isolated function post albums\-all\-2(Album[] albums, http:Request req, string? query = (), typedesc<Album|CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    @MethodImpl {name: "postAlbumsAll2Impl"}
    remote isolated function postAlbumsAll2(Album[] albums, http:Request req, string? query = (), typedesc<Album|CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    private isolated function postAlbumsAll2Impl(Album[] albums, http:Request req, string? query, typedesc<Album|CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload> targetType) returns Album|CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload|error {
        string resourcePath = string `/albums-all`;
        return self.clientEp->post(resourcePath, albums, targetType = targetType);
    }

    @MethodImpl {name: "getAlbumsIdImpl"}
    remote isolated function getAlbumsId(string id, typedesc<Album|OkAlbum|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    @MethodImpl {name: "getAlbumsImpl"}
    remote isolated function getAlbums(string genre = "Rock", typedesc<Album[]|OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    @MethodImpl {name: "getAlbumsImpl1"}
    remote isolated function getAlbums1(string genre = "Rock", typedesc<Album[]|OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    remote isolated function getAlbums2(string genre = "Rock", typedesc<Album[]|OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    @MethodImpl {name: "getAlbumsImpl3"}
    remote isolated function getAlbums3(string genre = "Rock", typedesc<Album[]|OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invoke"} external;

    private isolated function getAlbumsImpl3(string genre, string 'type) returns Album[]|OkAlbumArray|NotFoundErrorMessage|BadRequestErrorPayload|error {
        return [];
    }
}
