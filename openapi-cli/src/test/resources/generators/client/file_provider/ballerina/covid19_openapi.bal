import  ballerina/http;

public type CountriesArr Countries[];

public type CountryInfoArr CountryInfo[];

# Here you can find documentation for COVID-19 REST API.
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "https://api-cov19.now.sh/") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, timeout: config.timeout, forwarded: config.forwarded, poolConfig: config.poolConfig, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, validation: config.validation};
        do {
            if config.http1Settings is ClientHttp1Settings {
                ClientHttp1Settings settings = check config.http1Settings.ensureType(ClientHttp1Settings);
                httpClientConfig.http1Settings = {...settings};
            }
            if config.http2Settings is http:ClientHttp2Settings {
                httpClientConfig.http2Settings = check config.http2Settings.ensureType(http:ClientHttp2Settings);
            }
            if config.cache is http:CacheConfig {
                httpClientConfig.cache = check config.cache.ensureType(http:CacheConfig);
            }
            if config.responseLimits is http:ResponseLimitConfigs {
                httpClientConfig.responseLimits = check config.responseLimits.ensureType(http:ResponseLimitConfigs);
            }
            if config.secureSocket is http:ClientSecureSocket {
                httpClientConfig.secureSocket = check config.secureSocket.ensureType(http:ClientSecureSocket);
            }
            if config.proxy is http:ProxyConfig {
                httpClientConfig.proxy = check config.proxy.ensureType(http:ProxyConfig);
            }
        }
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
        return;
    }
    # Returns information about all countries
    #
    # + return - A list of countries with all informtion included.
    remote isolated function getCovidinAllCountries() returns CountriesArr|error {
        string resourcePath = string `/api`;
        CountriesArr response = check self.clientEp-> get(resourcePath);
        return response;
    }
    # List of all countries with COVID-19 cases
    #
    # + return - Default response with array of strings
    remote isolated function getCountryList() returns CountryInfoArr|error {
        string resourcePath = string `/api/v1/countries/list/`;
        CountryInfoArr response = check self.clientEp-> get(resourcePath);
        return response;
    }
    # Returns information about country. Pass country name as a parameter. Country name is case insensitive. For example – https://api-cov19.now.sh/api/countries/netherlands
    #
    # + country - String Name of the country to get
    # + return - A list of countries with all informtion included.
    remote isolated function getCountryByName(string country) returns Country|error {
        string resourcePath = string `/api/countries/${country}`;
        Country response = check self.clientEp-> get(resourcePath);
        return response;
    }
}
