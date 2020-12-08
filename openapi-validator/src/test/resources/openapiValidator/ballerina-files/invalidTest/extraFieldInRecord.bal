import ballerina/http;

type Category record {
    string id;
    string name;
};
type Tag record {
    int id;
    string name;
};

type ExtraFieldInRecord record {
    int id;
    string name;
    string status;
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
