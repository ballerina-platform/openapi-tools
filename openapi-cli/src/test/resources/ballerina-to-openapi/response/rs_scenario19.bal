import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    resource function get pet() returns http:Response {
        http:Response res = new;
        return res;
    }

    resource function get pets() returns json {
        json ok = "Apple";
        return ok;
    }
}
