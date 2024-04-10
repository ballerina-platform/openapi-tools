import ballerina/http;

table<Album> key(id) albums = table [
    {id: "1", name: "The Dark Side of the Moon", artist: "Pink Floyd", genre: "Progressive Rock"},
    {id: "2", name: "Back in Black", artist: "AC/DC", genre: "Hard Rock"},
    {id: "3", name: "The Wall", artist: "Pink Floyd", genre: "Progressive Rock"}
];

service /api on new http:Listener(9999) {

    resource function get albums/[string id]() returns OkAlbum|NotFoundErrorMessage {
        if albums.hasKey(id) {
            return {
                body: albums.get(id),
                headers: {user\-id: "user-1", req\-id: 1}
            };
        }
        return {
            body: {"albumId": id, message: "Album not found"},
            headers: {user\-id: "user-1", req\-id: 1}
        };
    }

    resource function get albums(string genre) returns OkAlbumArray|NotFoundErrorMessage {
        Album[] selectedAlbums = from Album album in albums
            where album.genre == genre
            select album;
        if selectedAlbums.length() == 0 {
            return {
                body: {"genre": genre, message: "No albums found"},
                headers: {user\-id: "user-1", req\-id: 1}
            };
        }
        return {
            body: selectedAlbums,
            headers: {user\-id: "user-1", req\-id: 1}
        };
    }

    resource function get albums\-all() returns OkAlbumArray|NotFoundErrorMessage {
        Album[] albumsArr = albums.toArray();
        if albumsArr.length() == 0 {
            return {
                body: {"message": "No albums found"},
                headers: {user\-id: "user-1", req\-id: 1}
            };
        }
        return {
            body: albumsArr,
            headers: {user\-id: "user-1", req\-id: 1}
        };
    }
}
