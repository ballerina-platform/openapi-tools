import ballerina/http;

type Person record {
    int id ;
    string name;
};

// By default, Ballerina exposes an HTTP service via HTTP/1.1.
service /payloadV on new http:Listener(9090) {
    resource function get pets () returns record {| *Person; string body;|} {
        return {
            id: 1,
            name: "",
            body: "test"
        };
    }
}
