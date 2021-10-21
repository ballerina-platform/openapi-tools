import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    resource function get pets(string pet = "hello") returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function get ping(int offset = 5) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function get ping02(decimal offset = 100.08) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function get ping03(boolean offset = true) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function get ping04(float offset = 100.08) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function get ping05(int[] offset = [2, 1, 3, 4]) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function get ping06(int? offset = ()) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function get ping07(map<json>? offset = {"x": {"id": "sss"}}) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }

    # Mock resource function
    #
    # + offset - Mock query parameter
    # + return - Return Value Description
    resource function get ping08(map<json> offset = {"x": {"id": "sss"}}) returns http:Ok {
         http:Ok ok = {body: ()};
         return ok;
    }
}
