import ballerina/http;

public type Status record {
    int observedGeneration;
};

public type Pod record {
    *Resource;
    ResourceKind kind = "Pod";
    PodSpec spec;
};

public type PodSpec record {
    string nodeName;
};

public type ResourceBase record {
    string group;
    string version;
    ResourceKind kind;
    Metadata metadata;
};

public type Metadata record {
    string name;
    string displayName?;
    string description?;
};

public type Resource record {
    *ResourceBase;
    record {} spec;
    Status status?;
};

public type ServiceSpec record {
    string clusterIP;
};

public type Service record {
    *Resource;
    ResourceKind kind = "Service";
    ServiceSpec spec;
};

public type RecF record {|
    int f?;
|};

public type RecG record {
    *RecF;
    int g?;
};

public type RecD record {
    *RecA;
    string a = "aad";
    int d;
};

public type RecE record {
    *RecA;
    string a;
    string e;
};

public type RecB record {
    *RecA;
    int b;
};

public type RecC record {
    *RecA;
    string aa = "aa";
    int c;
};

public type RecA record {|
    string a = "a";
    string aa;
|};

public type RecL record {|
    string first\-name?;
    int id;
|};

public type ResourceKind "Service"|"Pod";

public type RecK record {
    *RecA;
    *RecF;
    *RecL;
    int k;
    string a;
    string first\-name;
};

public type RecH record {
    *RecG;
    string h;
    int f;
    int g;
};

public type RecI record {
    *RecG;
    int f = 10;
    string i;
    int g;
};

public type RecKAccepted record {|
    *http:Accepted;
    RecK body;
|};
