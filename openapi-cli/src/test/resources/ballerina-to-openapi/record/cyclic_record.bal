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

type ResponseError05 record {|
    int? id;
    ResponseError05? resError?;
|};

type ResponseError06 record {|
    int? id;
    ResponseError06[]? resError?;
|};

type ResponseError07 record {|
    int? id;
    ResponseError07[]? resError;
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
    resource function post pet05() returns ResponseError05|http:Accepted {
         http:Accepted accept = {body: ()};
         return accept;
    }
    resource function post pet06() returns ResponseError06|http:Accepted {
         http:Accepted accept = {body: ()};
         return accept;
    }
    resource function post pet07() returns ResponseError07|http:Accepted {
        http:Accepted accept = {body: ()};
        return accept;
    }
}
