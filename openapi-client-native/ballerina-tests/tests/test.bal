import ballerina/http;
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

    res = albumClient->getAlbumsAll();
    if res is OkAlbumArray {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testResourceMethod8() {
    http:Request req = new;
    Album newAlbum = {id: "4", name: "The Wall", artist: "Pink Floyd", genre: "Progressive Rock"};
    req.setJsonPayload(newAlbum);
    CreatedAlbum|error res = albumClient->/albums.post(req);
    CreatedAlbum expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: {id: "4", name: "The Wall", artist: "Pink Floyd", genre: "Progressive Rock"}
    };
    if res is CreatedAlbum {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }

    newAlbum = {id: "14", name: "The Wall", artist: "Pink Floyd", genre: "Progressive Rock"};
    expected.body = newAlbum;
    req.setJsonPayload(newAlbum);
    res = albumClient->postAlbums(req);
    if res is CreatedAlbum {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testResourceMethod9() {
    http:Request req = new;
    Album existingAlbum = {id: "1", name: "The Dark Side of the Moon", artist: "Pink Floyd", genre: "Progressive Rock"};
    req.setJsonPayload(existingAlbum);
    CreatedAlbum|ConflictAlbum|error res = albumClient->/albums.post(req);
    ConflictAlbum expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: {message: "Album already exists", "albumId": "1"}
    };
    if res is ConflictAlbum {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }

    res = albumClient->postAlbums(req);
    if res is ConflictAlbum {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testResourceMethod10() {
    http:Request req = new;
    json notAnAlbum = {id: "4", name: "The Wall", artist: "Pink Floyd"};
    req.setJsonPayload(notAnAlbum);
    CreatedAlbum|ConflictAlbum|BadRequestErrorPayload|error res = albumClient->/albums.post(req);
    test:assertTrue(res is BadRequestErrorPayload);

    res = albumClient->postAlbums(req);
    test:assertTrue(res is BadRequestErrorPayload);
}

@test:Config {}
function testResourceMethod11() {
    http:Request req = new;
    Album newAlbum = {id: "5", name: "The Wall", artist: "Pink Floyd", genre: "Progressive Rock"};
    req.setJsonPayload(newAlbum);
    CreatedAlbum|error res = albumClient->/albums\-1/a/[3].post(req);
    CreatedAlbum expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: {id: "5", name: "The Wall", artist: "Pink Floyd", genre: "Progressive Rock"}
    };
    if res is CreatedAlbum {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }

    newAlbum = {id: "15", name: "The Wall", artist: "Pink Floyd", genre: "Progressive Rock"};
    expected.body = newAlbum;
    req.setJsonPayload(newAlbum);
    res = albumClient->postAlbums1("a", 3, req);
    if res is CreatedAlbum {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testResourceMethod12() {
    http:Request req = new;
    Album existingAlbum = {id: "1", name: "The Dark Side of the Moon", artist: "Pink Floyd", genre: "Progressive Rock"};
    req.setJsonPayload(existingAlbum);
    CreatedAlbum|ConflictAlbum|error res = albumClient->/albums\-1/a/[3].post(req);
    ConflictAlbum expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: {message: "Album already exists", "albumId": "1"}
    };
    if res is ConflictAlbum {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }

    res = albumClient->postAlbums1("a", 3, req);
    if res is ConflictAlbum {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testResourceMethod13() {
    http:Request req = new;
    json notAnAlbum = {id: "4", name: "The Wall", artist: "Pink Floyd"};
    req.setJsonPayload(notAnAlbum);
    CreatedAlbum|ConflictAlbum|BadRequestErrorPayload|error res = albumClient->/albums\-1/a/[3].post(req);
    test:assertTrue(res is BadRequestErrorPayload);

    res = albumClient->postAlbums1("a", 3, req);
    test:assertTrue(res is BadRequestErrorPayload);
}

@test:Config {}
function testResourceMethod14() {
    Album[] newAlbums = [{id: "6", name: "The Wall", artist: "Pink Floyd", genre: "Progressive Rock"}];
    CreatedAlbumArray|error res = albumClient->/albums\-all/a.post(newAlbums, query = "q");
    CreatedAlbumArray expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: [{id: "6", name: "The Wall", artist: "Pink Floyd", genre: "Progressive Rock"}]
    };
    if res is CreatedAlbumArray {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }

    newAlbums = [{id: "16", name: "The Wall", artist: "Pink Floyd", genre: "Progressive Rock"}];
    expected.body = newAlbums;
    res = albumClient->postAlbumsAll("a", newAlbums, query = "q");
    if res is CreatedAlbumArray {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

function testResourceMethod15() {
    Album[] existingAlbums = [{id: "1", name: "The Dark Side of the Moon", artist: "Pink Floyd", genre: "Progressive Rock"}];
    CreatedAlbumArray|ConflictAlbum|error res = albumClient->/albums\-all/a.post(existingAlbums);
    ConflictAlbum expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: {message: "Album already exists", "albumId": "1"}
    };
    if res is ConflictAlbum {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }

    res = albumClient->postAlbumsAll("a", existingAlbums);
    if res is ConflictAlbum {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testResourceMethod16() {
    http:Request req = new;
    Album[] newAlbums = [{id: "7", name: "The Wall", artist: "Pink Floyd", genre: "Progressive Rock"}];
    req.setJsonPayload(newAlbums);
    CreatedAlbumArray|error res = albumClient->/albums\-all\-1.post(req, query = "q");
    CreatedAlbumArray expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: [{id: "7", name: "The Wall", artist: "Pink Floyd", genre: "Progressive Rock"}]
    };
    if res is CreatedAlbumArray {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }

    newAlbums = [{id: "17", name: "The Wall", artist: "Pink Floyd", genre: "Progressive Rock"}];
    expected.body = newAlbums;
    req.setJsonPayload(newAlbums);
    res = albumClient->postAlbumsAll1(req, query = "q");
    if res is CreatedAlbumArray {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testResourceMethod17() {
    http:Request req = new;
    Album[] existingAlbums = [{id: "1", name: "The Dark Side of the Moon", artist: "Pink Floyd", genre: "Progressive Rock"}];
    req.setJsonPayload(existingAlbums);
    CreatedAlbumArray|ConflictAlbum|error res = albumClient->/albums\-all\-1.post(req, query = "q");
    ConflictAlbum expected = {
        mediaType: "application/json",
        headers: {user\-id: "user-1", req\-id: 1},
        body: {message: "Album already exists", "albumId": "1"}
    };
    if res is ConflictAlbum {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }

    res = albumClient->postAlbumsAll1(req, query = "q");
    if res is ConflictAlbum {
        test:assertEquals(res.mediaType, expected.mediaType, "mediaType did not match");
        test:assertEquals(res.headers, expected.headers, "headers did not match");
        test:assertEquals(res.body, expected.body, "body did not match");
    } else {
        test:assertFail("invalid response type");
    }
}

@test:Config {}
function testResourceMethod18() {
    http:Request req = new;
    json notAnAlbum = [{id: "4", name: "The Wall", artist: "Pink Floyd"}];
    req.setJsonPayload(notAnAlbum);
    CreatedAlbumArray|ConflictAlbum|BadRequestErrorPayload|error res = albumClient->/albums\-all\-1.post(req, query = "q");
    test:assertTrue(res is BadRequestErrorPayload);

    res = albumClient->postAlbumsAll1(req, query = "q");
    test:assertTrue(res is BadRequestErrorPayload);
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
