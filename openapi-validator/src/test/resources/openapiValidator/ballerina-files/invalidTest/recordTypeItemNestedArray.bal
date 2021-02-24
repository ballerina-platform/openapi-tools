import ballerina/http;

type Tag record {
    string id;
    string name;
};
type RecordTypeArray record {
    int id;
    Tag [][][] tags;
};

service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller,
        http:Request req, RecordTypeArray body ) returns error? {
    }
}
