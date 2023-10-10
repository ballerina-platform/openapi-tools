import ballerina/http;

public type ABC record {
    int id;
    string name;
};
type UnionType ABC|xml;

service /payloadV on new http:Listener(0) {
    resource function post path(ABC? payload) {
    }
    //This needs to be fixed with separated PR
    resource function post path1(UnionType payload) {
    }
    resource function post path2(ABC|xml payload) {
    }
    resource function post path3(ABC|http:Remote|xml? payload) {
    }
    resource function post path4(@http:Payload ABC|string|xml payload) {
    }
    //oneOF- scenarios for application/json
    resource function post path5(@http:Payload ABC|int payload) {
    }
    resource function post path6(@http:Payload int|string payload) {
    }
    resource function post path7(@http:Payload map<json>|int[] payload) {
    }
    resource function post path8(@http:Payload map<int>|string payload) {
    }
    resource function post path9(@http:Payload map<int>|map<string> payload) {
    }

    // //negative - skip
    // resource function post path10(ABC|string payload) {
    // }
    // resource function post path11(int|string payload) {
    // }
    // resource function post path12(map<int>|string payload) {
    // }
}
