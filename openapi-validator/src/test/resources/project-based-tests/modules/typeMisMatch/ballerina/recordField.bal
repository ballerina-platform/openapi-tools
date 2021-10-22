import ballerina/http;

type TypeMisMatch record {
    string id;
    string username;
    string password;
    string phone;
    int userStatus;
};

service hello on new http:Listener(9090) {
    resource function post pet(@http:Payload TypeMisMatch payload) returns error? {
    }
}
