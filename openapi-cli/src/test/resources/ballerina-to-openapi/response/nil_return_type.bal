import ballerina/http;

service /payloadV on new http:Listener(9090) {
    resource function get path1() {
    };
    resource function get path_with_query(string id)  {
    };
    resource function get path_with_path/[string id]() {
    };
    resource function get path_with_headers(@http:Header string header) {
    };

    resource function post path1() {
    };
    resource function post path_with_query(string id)  {
    };
    resource function post path_with_path/[string id]() {
    };
    resource function post path_with_headers(@http:Header string header) {
    };
    resource function post path_with_request_body(@http:Payload string payload) {
    };

    resource function put path1() {
    };
    resource function put path_with_query(string id)  {
    };
    resource function put path_with_path/[string id]() {
    };
    resource function put path_with_headers(@http:Header string header) {
    };
    resource function put path_with_request_body(@http:Payload string payload) {
    };

    resource function patch path1() {
    };
    resource function patch path_with_query(string id)  {
    };
    resource function patch path_with_path/[string id]() {
    };
    resource function patch path_with_headers(@http:Header string header) {
    };
    resource function patch path_with_request_body(@http:Payload string payload) {
    };

    resource function delete path1() {
    };
    resource function delete path_with_query(string id)  {
    };
    resource function delete path_with_path/[string id]() {
    };
    resource function delete path_with_headers(@http:Header string header) {
    };
    resource function delete path_with_request_body(@http:Payload string payload) {
    };

    resource function head path1() {
    };
    resource function head path_with_query(string id)  {
    };
    resource function head path_with_path/[string id]() {
    };
    resource function head path_with_headers(@http:Header string header) {
    };

    resource function option path1() {
    };
    resource function option path_with_query(string id)  {
    };
    resource function option path_with_path/[string id]() {
    };
    resource function option path_with_headers(@http:Header string header) {
    };
    resource function option path_with_request_body(@http:Payload string payload) {
    };
}
