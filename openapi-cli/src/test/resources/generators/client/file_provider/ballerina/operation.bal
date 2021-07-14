import  ballerina/http;

#Here you can find documentation for COVID-19 REST API.
#
#+clientEp-Connector http endpoint
public client class Client {
    http:Client clientEp;
    # Client initialization.
    #
    # + clientConfig - Client configuration details
    # + serviceUrl - Connector server URL
    # + return -  Returns error at failure of client initialization
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https://api-cov19.now.sh/") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
    }
    #
    #+return-Default response with array of strings
    remote isolated function getCountryList() returns CountryInfo[]|error {
        string  path = string `/api/v1/countries/list/`;
        CountryInfo[] response = check self.clientEp-> get(path, targetType = CountryInfoArr);
        return response;
    }
}
