import ballerina/http;

// Create an endpoint with port 8080 for the mock backend services.
listener http:Listener backendEP = check new (8080);

// Define the load balance client endpoint to call the backend services.
http:LoadBalanceClient lbBackendEP = check new ({
        // Define the set of HTTP clients that need to be load balanced.
        targets: [
            {url: "http://localhost:8080/mock1"},
            {url: "http://localhost:8080/mock2"},
            {url: "http://localhost:8080/mock3"}
        ],

        timeout: 5
});

service /payloadV on new http:Listener(9090) {
  resource function get lb() returns string|error {
        string payload = check lbBackendEP->get("/");
        return payload;
    }
}
