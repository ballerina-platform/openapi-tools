type '200 record {
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

type Coord record {
    any lon?;
    any lat?;
};

type Weather record {
    int id?;
    string main?;
    string description?;
    string icon?;
};

type Main record {
    any temp?;
    int pressure?;
    int humidity?;
    any temp_min?;
    any temp_max?;
    any sea_level?;
    any grnd_level?;
};

type Wind record {
    any speed?;
    int deg?;
};

type Clouds record {
    int 'all?;
};

type Rain record {
    int '\3h?;
};

type Snow record {
    any '\3h?;
};

type Sys record {
    int 'type?;
    int id?;
    any message?;
    string country?;
    int sunrise?;
    int sunset?;
};
