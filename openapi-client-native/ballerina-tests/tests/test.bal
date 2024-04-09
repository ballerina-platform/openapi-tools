import ballerina/test;

final Client albumClient = check new;

@test:Config {}
function testResourceMethod1() {
    OkAlbum|NotFoundErrorMessage|error res = albumClient->/albums/'1;
    OkAlbum expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: {id: "1", name: "The Dark Side of the Moon", artist: "Pink Floyd", genre: "Progressive Rock"}
    };
    if res is OkAlbum {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testResourceMethod2() {
    OkAlbum|NotFoundErrorMessage|error res = albumClient->/albums/'101;
    NotFoundErrorMessage expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: {message: "Album not found", "albumId": "101"}
    };
    if res is NotFoundErrorMessage {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testResourceMethod3() {
    Album|error res = albumClient->/albums/'1();
    Album expected = {
        id: "1",
        name: "The Dark Side of the Moon",
        artist: "Pink Floyd",
        genre: "Progressive Rock"
    };
    if res is Album {
        test:assertEquals(res, expected, "response did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testResourceMethod4() {
    Album[]|error res = albumClient->/albums.get("Hard Rock");
    Album[] expected = [
        {id: "2", name: "Back in Black", artist: "AC/DC", genre: "Hard Rock"}
    ];
    if res is Album[] {
        test:assertEquals(res, expected, "response did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testResourceMethod5() {
    OkAlbumArray|NotFoundErrorMessage|error res = albumClient->/albums.get("Hard Rock");
    OkAlbumArray expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: [{id: "2", name: "Back in Black", artist: "AC/DC", genre: "Hard Rock"}]
    };
    if res is OkAlbumArray {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testResourceMethod6() {
    OkAlbumArray|NotFoundErrorMessage|error res = albumClient->/albums.get("Rock");
    NotFoundErrorMessage expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: {message: "No albums found", "genre": "Rock"}
    };
    if res is NotFoundErrorMessage {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testResourceMethod7() {
    OkAlbumArray|NotFoundErrorMessage|error res = albumClient->/albums\-all;
    OkAlbumArray expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: albums.toArray()
    };
    if res is OkAlbumArray {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testRemoteMethod1() {
    OkAlbum|NotFoundErrorMessage|error res = albumClient->getAlbumsId("1");
    OkAlbum expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: {id: "1", name: "The Dark Side of the Moon", artist: "Pink Floyd", genre: "Progressive Rock"}
    };
    if res is OkAlbum {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testRemoteMethod2() {
    OkAlbum|NotFoundErrorMessage|error res = albumClient->getAlbumsId("101");
    NotFoundErrorMessage expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: {message: "Album not found", "albumId": "101"}
    };
    if res is NotFoundErrorMessage {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testRemoteMethod3() {
    Album|error res = albumClient->getAlbumsId("1");
    Album expected = {
        id: "1",
        name: "The Dark Side of the Moon",
        artist: "Pink Floyd",
        genre: "Progressive Rock"
    };
    if res is Album {
        test:assertEquals(res, expected, "response did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testRemoteMethod4() {
    Album[]|error res = albumClient->getAlbums("Hard Rock");
    Album[] expected = [
        {id: "2", name: "Back in Black", artist: "AC/DC", genre: "Hard Rock"}
    ];
    if res is Album[] {
        test:assertEquals(res, expected, "response did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testRemoteMethod5() {
    OkAlbumArray|NotFoundErrorMessage|error res = albumClient->getAlbums("Hard Rock");
    OkAlbumArray expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: [{id: "2", name: "Back in Black", artist: "AC/DC", genre: "Hard Rock"}]
    };
    if res is OkAlbumArray {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testRemoteMethod6() {
    OkAlbumArray|NotFoundErrorMessage|error res = albumClient->getAlbums("Rock");
    NotFoundErrorMessage expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: {message: "No albums found", "genre": "Rock"}
    };
    if res is NotFoundErrorMessage {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testInvalidMethodInvocation() {
    OkAlbumArray|NotFoundErrorMessage|error res = albumClient->getAlbums1("Hard Rock");
    if res is error {
        test:assertTrue(res is ClientMethodInvocationError);
        test:assertEquals(res.message(), "client method invocation failed: No such method: getAlbumsImpl1");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testAnnotationNotFound() {
    OkAlbumArray|NotFoundErrorMessage|error res = albumClient->getAlbums2("Hard Rock");
    if res is error {
        test:assertTrue(res is ClientMethodInvocationError);
        test:assertEquals(res.message(), "error in invoking client remote method: Method implementation annotation not found");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testInvalidImplFunctionSignature() {
    OkAlbumArray|NotFoundErrorMessage|error res = albumClient->getAlbums3("Hard Rock");
    if res is error {
        test:assertTrue(res is ClientMethodInvocationError);
        test:assertTrue(res.message().includes("client method invocation failed: java.lang.ClassCastException: " +
        "class io.ballerina.runtime.internal.values.TypedescValueImpl cannot be cast to class io.ballerina.runtime.api.values.BString"));
    } else {
        test:assertFail("invalid response type");
    }
}
