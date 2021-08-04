import  ballerina/http;

#Here you can find documentation for COVID-19 REST API.
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return -  An error at the failure of client initialization
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https://api-cov19.now.sh/") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
    }
    #
    #+return-A list of countries with all informtion included.
    remote isolated function getCovidinAllCountries() returns Countries[]|error {
        string  path = string `/api`;
        Countries[] response = check self.clientEp-> get(path, targetType = CountriesArr);
        return response;
    }
}
