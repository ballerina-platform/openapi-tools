import  ballerina/http;

type CountryInfoArr CountryInfo[];

public client class Client {
    http:Client clientEp;
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https://api-cov19.now.sh/") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
    }
    remote isolated function getCountryList() returns CountryInfoArr|error {
        string  path = string `/api/v1/countries/list/`;
        CountryInfoArr response = check self.clientEp-> get(path, targetType = CountryInfoArr);
        return response;
    }
}
