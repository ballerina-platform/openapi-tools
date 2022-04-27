import ballerina/openapi;
import ballerina/http;


type Pet record {|
    int id;
    string name;
    string 'type;
|};
@openapi:ServiceInfo {
    contract:"unimplemented_media_type.yaml"
}

service / on new http:Listener(9090) {
    resource function get .() returns Pet | http:NotFound {
        return {
            body: {
                id: 1,
                name: "test",
                'type: "dog"
            }
        };
    }
}
