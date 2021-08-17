import ballerina/http;

listener http:Listener helloEp = new (9090);

type Person record {
    int id ;
    string name;
};

service /payloadV on helloEp {
    resource function get pets () returns record {| int id; Person body;|} {
            return { id: 1, body: {
                id: 0,
                name: ""
            }};
        }
}

