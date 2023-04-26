import ballerina/http;

public listener http:Listener ep_with_host = new (443, config = {host: "http://petstore.openapi.io"});
