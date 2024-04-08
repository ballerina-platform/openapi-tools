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

    public isolated function init(string serviceUrl = "http://localhost:9090/api") returns error? {
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
