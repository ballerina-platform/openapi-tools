public client class Client {
    public http:Client clientEp;
    public function init(string serviceUrl = "http://localhost:9090/v1", http:ClientConfiguration httpClientConfig = {})
    returns error? {
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
    }
}
