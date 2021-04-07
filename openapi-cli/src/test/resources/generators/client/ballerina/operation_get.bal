import  ballerina/http;

public client class Client {
    public http:Client clientEp;
    public function init(string serviceUrl = "https://petstore.swagger.io:443/v2", http:ClientConfiguration httpClientConfig= {})
    returns error?{
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
    }
}
