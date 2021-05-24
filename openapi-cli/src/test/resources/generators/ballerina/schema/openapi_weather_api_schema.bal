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
    float lon?;
    float lat?;
};

public type Weather record {
    int id?;
    string main?;
    string description?;
    string icon?;
};

public type Main record {
    float temp?;
    int pressure?;
    int humidity?;
    float temp_min?;
    float temp_max?;
    float sea_level?;
    float grnd_level?;
};

public type Wind record {
    float speed?;
    int deg?;
};

public type Clouds record {
    int 'all?;
};

public type Rain record {
    int '\3h?;
};

public type Snow record {
    float '\3h?;
};

public type Sys record {
    int 'type?;
    int id?;
    float message?;
    string country?;
    int sunrise?;
    int sunset?;
};
