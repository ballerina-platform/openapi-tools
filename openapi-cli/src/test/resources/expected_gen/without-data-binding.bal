import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service /v1 on ep0 {
    resource function get coupons/[Coupon couponCode](http:Caller caller, http:Request request) returns error? {
    }
}
