import  ballerina/http;

type CountriesArr Countries[];

public client class Client {
    http:Client clientEp;
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https://api-cov19.now.sh/") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
    }
    remote isolated function getCovidinAllCountries() returns CountriesArr|error {
        string  path = string `/api`;
        CountriesArr response = check self.clientEp-> get(path, targetType = CountriesArr);
        return response;
    }
}
