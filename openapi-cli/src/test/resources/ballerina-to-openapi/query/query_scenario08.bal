import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    string y = "hello";
    resource function get pets(string pet = y) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function get pet(string pet = "hi" + y) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function get ping(int offest = 10 / 2) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function get ping02(string limitV = getHeader()) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
}

function getHeader() returns string {
    return "query";
}
