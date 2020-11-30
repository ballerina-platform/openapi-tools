import ballerina/http;
import ballerina/log;

listener http:Listener helloEp = new (9090);

service hello on helloEp {
    resource function hi(http:Caller caller, http:Request request) {
        http:Response res = new;
        res.setPayload("Hello World!");

        var result = caller->respond(res);
        if (result is error) {
           log:printError("Error when responding", result);
        }
    }
}


//
//type User record {
//    string id;
//    string username;
//    string firstName;
//    string lastName;
//    string email;
//    string password;
//    string phone;
//    int userStatus;
//};
//
//service hello on new http:Listener(9090) {
//    resource function sayHello(http:Caller caller,
//        http:Request req, User body ) returns error? {
//    }
//}
