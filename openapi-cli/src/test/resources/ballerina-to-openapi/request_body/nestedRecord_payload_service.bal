import  ballerina/http;

listener http:Listener helloEp = new (9090);

type Pet record {
    int id;
    string name;
    string tag?;
 };

 type Dog record {
     Pet perant;
     boolean bark;
  };
 service /payloadV on helloEp {
     resource function post hi(http:Caller caller, http:Request request, @http:Payload {} Dog payload) {
     }
 }


# Covid-19 status of the given country
public type CovidCountry record {
    # Last updated timestamp
    decimal updated?;
    # Country name
    string country?;
    # Country information
    record  { # Country Id
        decimal _id?;
        # Country ISO2 code
        string iso2?;
        # Country ISO3 code
        string iso3?;
        # Latitude
        decimal lat?;
        # Longtitude
        decimal long?; # URL for the country flag
        string flag?;}  countryInfo?;
    # Total cases
    decimal cases?;
    # Today cases
    decimal todayCases?;
    # Total deaths
    decimal deaths?;
    # Today deaths
    decimal todayDeaths?;
    # Total recovered
    decimal recovered?;
    # Today recovered
    decimal todayRecovered?;
    # Active cases
    decimal active?;
    # Critical cases
    decimal critical?;
    # Cases per one million
    decimal casesPerOneMillion?;
    # Deaths per one million
    decimal deathsPerOneMillion?;
    # Total number of Covid-19 tests administered
    decimal tests?;
    # Covid-19 tests for one million
    decimal testsPerOneMillion?;
    # Total population
    decimal population?;
    # Continent name
    string continent?;
    # One case per people
    decimal oneCasePerPeople?;
    # One death per people
    decimal oneDeathPerPeople?;
    # One test per people
    decimal oneTestPerPeople?;
    # Actove cases per one million
    decimal activePerOneMillion?;
    # Recovered cases per one million
    decimal recoveredPerOneMillion?;
    # Critical cases per one million
    decimal criticalPerOneMillion?;
};

