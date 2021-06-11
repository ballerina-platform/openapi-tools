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

public type Coord record {
    decimal lon?;
    decimal lat?;
};

public type Weather record {
    int id?;
    string main?;
    string description?;
    string icon?;
};

public type Main record {
    decimal temp?;
    int pressure?;
    int humidity?;
    decimal temp_min?;
    decimal temp_max?;
    decimal sea_level?;
    decimal grnd_level?;
};

public type Wind record {
    decimal speed?;
    int deg?;
};

public type Clouds record {
    int 'all?;
};

public type Rain record {
    int '\3h?;
};

public type Snow record {
    decimal '\3h?;
};

public type Sys record {
    int 'type?;
    int id?;
    decimal message?;
    string country?;
    int sunrise?;
    int sunset?;
};
