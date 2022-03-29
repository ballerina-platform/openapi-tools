import ballerina/http;

# Description
type Pet\-Task record {
    # Field id Description
    int pet\-id;
    # Field type Description
    string[] pet\-types?;
    # Pet store
    Pet\-Store store?;
};

type Pet\-Store record {
    int id;
};

service /v1/abc\-hello on new http:Listener(9090) {
    # Description
    #
    # + path\-param - Path Parameter Description
    # + q\-paramter - Query Parameter Description
    # + return - Return Value Description
    resource function get say\-hello/[string path\-param](string q\-paramter) returns Pet\-Task {
        Pet\-Task p = {
            pet\-id: 1
        };
        return p;
    }

    # Description
    #
    # + req\-body - Body Parameter Description
    # + x\-client - Header Parameter Description
    resource function post v2/say\-hello(@http:Payload Pet\-Task req\-body, @http:Header string x\-client) {

    }
}
type Pet record {
    string 'type;
    int id;
};

type Offset record {
    # pet type
    string 'type;
    int id;
    Pet 'join;
};
service /'limit on new http:Listener(9090) {
    # Query parameter
    #
    # + 'limit - QParameter Description
    resource function get steps/'from/date(string 'limit) returns string|error {
        return "Hello";
    }
    # Header parameter
    #
    # + 'limit - HParameter Description
    resource function get steps/[int 'join](@http:Header string 'limit) returns string|error {
        return "Hello";
    }

    resource function post steps(@http:Payload Offset payload) returns string|error {
        return "Hello";
    }
    resource function put พิมพ์ชื่อ(string ชื่อ) {

    }
    resource function get พิมพ์ชื่อ(string ชื่\u{E2D}) {

    }
}
