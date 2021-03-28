import ballerina/http;

type RecordTypeArray record {
    int id;
    int [] tags;
};

service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller,
        http:Request req, RecordTypeArray body ) returns error? {
    }
}
