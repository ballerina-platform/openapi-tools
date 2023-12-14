import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service /v1 on ep0 {
    resource function get coupons/[string couponCode]/[int id]/[string limits](http:Caller caller, http:Request request) returns error? {
    }
}