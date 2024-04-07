import ballerina/http;

# weather
public type Weather record {
    # Weather condition id
    int:Signed32 id?;
    # Group of weather parameters (Rain, Snow, Extreme etc.)
    string main?;
    # Weather condition within the group
    string description?;
    # Weather icon id
    string icon?;
};

# coord
public type Coord record {
    # City geo location, longitude
    decimal lon?;
    # City geo location, latitude
    decimal lat?;
};

# wind
public type Wind record {
    # Wind speed. Unit Default: meter/sec, Metric: meter/sec, Imperial: miles/hour.
    decimal speed?;
    # Wind direction, degrees (meteorological)
    int:Signed32 deg?;
};

# rain
public type Rain record {
    # Rain volume for the last 3 hours
    int:Signed32 '3h?;
};

# cloud
public type Clouds record {
    # Cloudiness, %
    int:Signed32 all?;
};

# snow
public type Snow record {
    # Snow volume for the last 3 hours
    decimal '3h?;
};

public type NotFoundString record {|
    *http:NotFound;
    string body;
    map<string|string[]> headers;
|};

public type '200 record {
    Coord coord?;
    Weather[] weather?;
    string base?;
    Main main?;
    int visibility?;
    Wind wind?;
    Clouds clouds?;
    Rain rain?;
    Snow snow?;
    int:Signed32 dt?;
    Sys sys?;
    int:Signed32 id?;
    string name?;
    int:Signed32 cod?;
};

# sys
public type Sys record {
    # Internal parameter
    int:Signed32 'type?;
    # Internal parameter
    int:Signed32 id?;
    # Internal parameter
    decimal message?;
    # Country code (GB, JP etc.)
    string country?;
    # Sunrise time, unix, UTC
    int:Signed32 sunrise?;
    # Sunset time, unix, UTC
    int:Signed32 sunset?;
};

# tests
public type Main record {
    # Temperature. Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
    decimal temp?;
    # Atmospheric pressure (on the sea level, if there is no sea_level or grnd_level data), hPa
    int:Signed32 pressure?;
    # Humidity, %
    int:Signed32 humidity?;
    # Minimum temperature at the moment. This is deviation from current temp that is possible for large cities and megalopolises geographically expanded (use these parameter optionally). Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
    decimal temp_min?;
    # Maximum temperature at the moment. This is deviation from current temp that is possible for large cities and megalopolises geographically expanded (use these parameter optionally). Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
    decimal temp_max?;
    # Atmospheric pressure on the sea level, hPa
    decimal sea_level?;
    # Atmospheric pressure on the ground level, hPa
    decimal grnd_level?;
};
