import ballerina/http;

public type clientConfig record {
    string serviceUrl;
    http:ClientConfiguration httpClientConfig;
};

public client class 'client {
    public http:Client clientEp;
    public clientConfig config;

    public function init(clientConfig config) {
        http:Client httpEp = checkpanic new(config.serviceUrl, {auth: config.httpClientConfig.auth, cache:
            config.httpClientConfig.cache});
        self.clientEp = httpEp;
        self.config = config;
    }
}

