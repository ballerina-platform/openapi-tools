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
