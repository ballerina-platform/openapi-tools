import ballerina/http;
import ballerina/openapi;


type Pet record {
    int id;
    string name; // extra feild
    string[] tags;
    Type 'type;
};

type Type record {
    string id;
    string 'type;

};

@openapi:ServiceInfo {
    contract: "record.yaml"
}
service on new http:Listener(8990) {
    // record type mismatch
    resource function post pet (@http:Payload Pet payload) {
    }

    //oas->ballerina mis rb
    resource function put pet (@http:Payload Pet payload) {
    }

    //ballerina->oas mis rb
    resource function post pet02 () {
    }

}
