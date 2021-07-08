# Successful response
public type CurrentWeatherDataResponse record {
    # coord
    Coord coord?;
    # (more info Weather condition codes)
    Weather[] weather?;
    # Internal parameter
    string base?;
    # tests
    Main main?;
    # Visibility, meter
    int visibility?;
    # wind
    Wind wind?;
    # cloud
    Clouds clouds?;
    # rain
    Rain rain?;
    # snow
    Snow snow?;
    # Time of data calculation, unix, UTC
    int dt?;
    # sys
    Sys sys?;
    # City ID
    int id?;
    # name
    string name?;
    # Internal parameter
    int cod?;
};
