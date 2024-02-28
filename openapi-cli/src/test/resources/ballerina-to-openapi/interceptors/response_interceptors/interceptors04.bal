import ballerina/http;

type Order record {
    int id;
    string name;
};

service class ResponseInterceptor {
    *http:ResponseInterceptor;

    remote function interceptResponse(http:Request req, http:RequestContext ctx) returns Order[]|http:NotFound|http:NextService? {
        return [];
    }
}

service http:InterceptableService /payloadV on new http:Listener(9090) {

    public function createInterceptors() returns [ResponseInterceptor] {
        return [];
    }

    resource function get greet() returns string {
        return "Hello, World!";
    }
}
