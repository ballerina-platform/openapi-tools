import  ballerina/http;

public type CountriesArr Countries[];

public type CountryInfoArr CountryInfo[];

# Here you can find documentation for COVID-19 REST API.
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
    # Returns information about all countries
    #
    # + return - A list of countries with all informtion included.
    remote isolated function getCovidinAllCountries() returns CountriesArr|error {
        string  path = string `/api`;
        CountriesArr response = check self.clientEp-> get(path, targetType = CountriesArr);
        return response;
    }
    # List of all countries with COVID-19 cases
    #
    # + return - Default response with array of strings
    remote isolated function getCountryList() returns CountryInfoArr|error {
        string  path = string `/api/v1/countries/list/`;
        CountryInfoArr response = check self.clientEp-> get(path, targetType = CountryInfoArr);
        return response;
    }
    # Returns information about country. Pass country name as a parameter. Country name is case insensitive. For example – https://api-cov19.now.sh/api/countries/netherlands
    #
    # + country - String Name of the country to get
    # + return - A list of countries with all informtion included.
    remote isolated function getCountryByName(string country) returns Country|error {
        string  path = string `/api/countries/${country}`;
        Country response = check self.clientEp-> get(path, targetType = Country);
        return response;
    }
}
