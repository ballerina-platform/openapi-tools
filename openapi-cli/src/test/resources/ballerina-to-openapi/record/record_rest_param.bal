import ballerina/http;

type User record {
    int id;
    string name;
};

type UserClosed record {|
    int id;
    string name;
|};

type UserRestString record {|
    int id;
    string name;
    string...;
|};

type UserRestMap record {|
    int id;
    string name;
    map<string>...;
|};

type Property record {|
    string name;
    string value;
|};

type UserRestProperty record {|
    int id;
    string name;
    Property...;
|};

type UserRestMapProperty record {|
    int id;
    string name;
    map<Property>...;
|};

service /payloadV on new http:Listener(9090) {

    resource function get path1(User user) {
    }

    resource function get path2(@http:Header UserClosed user) {
    }

    resource function post path3(UserRestString user) {
    }

    resource function post path4(@http:Query UserRestMap user1, UserClosed user2) {
    }

    resource function post path5(UserRestProperty user1, @http:Header UserClosed user2) {
    }

    resource function post path6(@http:Payload UserRestMapProperty user1, UserClosed user2) {
    }
}
