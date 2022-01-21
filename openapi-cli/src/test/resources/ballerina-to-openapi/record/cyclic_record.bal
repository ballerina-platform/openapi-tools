import ballerina/http;

type ResponseError record {|
    int? id;
    ResponseError resError?;
|};

type ResponseError02 record {|
    int? id;
    ResponseError02|string resError?;
|};

type ResponseError03 record {|
    int? id;
    ResponseError03[] resError?;
|};

type ResponseError04 record {|
    int? id;
    ResponseError04[][] resError?;
|};

listener http:Listener ep0 = new (443, config = {host: "petstore.swagger.io"});

service /payloadV on ep0 {
    resource function post pet() returns ResponseError|http:Accepted {
        http:Accepted accept = {body: ()};
        return accept;
    }
    resource function post pet02() returns ResponseError02|http:Accepted {
        http:Accepted accept = {body: ()};
        return accept;
    }
    resource function post pet03() returns ResponseError03|http:Accepted {
        http:Accepted accept = {body: ()};
        return accept;
    }
    resource function post pet04() returns ResponseError04|http:Accepted {
        http:Accepted accept = {body: ()};
        return accept;
    }
}
