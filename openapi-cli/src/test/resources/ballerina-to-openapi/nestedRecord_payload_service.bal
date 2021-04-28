import  ballerina/http;

//listener http:Listener helloEp = new (9090);
//
//type Pet record {
//    int id;
//    string name;
//    string tag?;
// };
//
// type Dog record {
//     Pet perant;
//     boolean bark;
//  };
// service /payloadV on helloEp {
//     resource function post hi(http:Caller caller, http:Request request, @http:Payload {} Dog payload) {
//     }
// }

public client class Client {
    public http:Client clientEp;
    public function init(string serviceUrl = "http://api.openweathermap.org/data/2.5/", http:ClientConfiguration httpClientConfig =
                         {}) returns error? {
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
    }

    @display {label: "Current Weather"}
    remote function currentWeatherData(string? q, string? id, string? lat, string? lon, string? zip, string? units,
                                       string? lang, string? mode) returns '200|error {
        string path = string `/weather`;
        map<anydata> queryParam = {
            q: q,
            id: id,
            lat: lat,
            lon: lon,
            zip: zip,
            units: units,
            lang: lang,
            mode: mode
        };
        path = path + getPathForQueryParam(queryParam);
        '200 response = check self.clientEp->get(path, targetType = '200);
        return response;
    }
}
