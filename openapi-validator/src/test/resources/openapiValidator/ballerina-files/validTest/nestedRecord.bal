import ballerina/http;

type Category record {
    string id;
    string name;
};
type Tag record {
    int id;
    string name;
};

type NestedRecord record {
    int id;
    Category category;
    string name;
    string photoUrls;
    //Tag [] tags;
    string status;
};

service hello on new http:Listener(9090) {
    resource function get student (NestedRecord body) {
    }
    //resource function get student (@http:Payload NestedRecord body) {
    //}
//    //resource function sayHello(http:Caller caller,
//    //    http:Request req, NestedRecord body ) returns error? {
//    //}
}
