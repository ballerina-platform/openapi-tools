#
#+coord-Field Description
#+weather-(more info Weather condition codes)
#+base-Internal parameter
#+main-Field Description
#+visibility-Visibility, meter
#+wind-Field Description
#+clouds-Field Description
#+rain-Field Description
#+snow-Field Description
#+dt-Time of data calculation, unix, UTC
#+sys-Field Description
#+id-City ID
#+name-Field Description
#+cod-Internal parameter
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
    int dt?;
    Sys sys?;
    int id?;
    string name?;
    int cod?;
};

#
#+lon-City geo location, longitude
#+lat-City geo location, latitude
public type Coord record {
    decimal lon?;
    decimal lat?;
};

#
#+id-Weather condition id
#+main-Group of weather parameters (Rain, Snow, Extreme etc.)
#+description-Weather condition within the group
#+icon-Weather icon id
public type Weather record {
    int id?;
    string main?;
    string description?;
    string icon?;
};

#
#+temp-Temperature. Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
#+pressure-Atmospheric pressure (on the sea level, if there is no sea_level or grnd_level data), hPa
#+humidity-Humidity, %
#+temp_min-Minimum temperature at the moment. This is deviation from current temp that is possible for large cities and megalopolises geographically expanded (use these parameter optionally). Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
#+temp_max-Maximum temperature at the moment. This is deviation from current temp that is possible for large cities and megalopolises geographically expanded (use these parameter optionally). Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.#+sea_level-Atmospheric pressure on the sea level, hPa
#+grnd_level-Atmospheric pressure on the ground level, hPa
public type Main record {
    decimal temp?;
    int pressure?;
    int humidity?;
    decimal temp_min?;
    decimal temp_max?;
    decimal sea_level?;
    decimal grnd_level?;
};

#
#+speed-Wind speed. Unit Default: meter/sec, Metric: meter/sec, Imperial: miles/hour.
#+deg-Wind direction, degrees (meteorological)
public type Wind record {
    decimal speed?;
    int deg?;
};
#
#+'all-Cloudiness, %
public type Clouds record {
    int 'all?;
};

#
#+'\3h-Rain volume for the last 3 hours
public type Rain record {
    int '\3h?;
};

#
#+'\3h-Snow volume for the last 3 hours
public type Snow record {
    decimal '\3h?;
};

#
#+'type-Internal parameter
#+id-Internal parameter
#+message-Internal parameter
#+country-Country code (GB, JP etc.)
#+sunrise-Sunrise time, unix, UTC
#+sunset-Sunset time, unix, UTC
public type Sys record {
    int 'type?;
    int id?;
    decimal message?;
    string country?;
    int sunrise?;
    int sunset?;
};
