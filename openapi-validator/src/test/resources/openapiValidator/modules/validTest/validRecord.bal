//type Order record {
//        int id;
//        int petId;
//        int quantity;
//        string shipDate;
//        string status;
//        boolean complete;
//};
//type Category record {
//    int id;
//    string name;
//};
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
//type Tag record {
//    int id;
//    string name;
//};
//type Pet record {
//    int id;
//    Category category;
//    string name;
//    string [] photoUrls;
//    Tag [] tags;
//    string status;
//};
//type ApiResponse record {
//    int code;
//    string 'type;
//    string message;
//};

service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller,
        http:Request req, User body ) returns error? {
    }
}