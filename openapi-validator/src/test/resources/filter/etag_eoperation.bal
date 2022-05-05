import ballerina/openapi;
import ballerina/http;

type Response record {|
    *http:Accepted;
    string body;
|};

@openapi:ServiceInfo {
     contract: "etag_eoperation.yaml",
     excludeTags: ["pet"],
     excludeOperations:["operation2"]
 }
service / on new http:Listener(9090) {
    resource function post pet() returns Response {
        return {
            body: "Testing"
        };
    }

    resource function get pet () returns http:Ok {
        return <http:Ok> {};
    }

    resource function delete pet ()returns string {
        return "Hello";
    }

    resource function put pet () {

    }
}
