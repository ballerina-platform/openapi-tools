import ballerina/http;

public type Genre record {|
    string name;
    string|string[] description;
    int iid;
|};

public type Album record {|
    int iid = -1;
    string title;
    string author;
    Genre genre = {
        name: "Unknown",
        description: "Unknown",
        iid: -1
    };
    string[] tags = ["tag1", "tag2"];
    (string|int)[] ratings = ["unrated", 5, 4];
    decimal price = 100.55;
    boolean available = true;
|};

@http:ServiceConfig {
    basePath: "/payloadV"
}
type AlbumService service object {
    *http:ServiceContract;

    resource function get albums/[string id](string q1 = "query1", int q2 = -1, @http:Header {name: "X-HEADER"} string h1 = "header1") returns Album;
};
