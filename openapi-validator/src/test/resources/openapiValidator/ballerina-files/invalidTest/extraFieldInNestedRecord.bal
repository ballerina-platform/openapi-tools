import ballerina/http;

type Category record {
    int id;
    string name;
    string tag;
};

type ExtraFieldInRecord record {
    int id;
    string name;
    Category category;
};

service hello on new http:Listener(9090) {
    resource function get student (ExtraFieldInRecord body) {
    }
    //resource function get student (@http:Payload NestedRecord body) {
    //}
//    //resource function sayHello(http:Caller caller,
//    //    http:Request req, NestedRecord body ) returns error? {
//    //}
}
