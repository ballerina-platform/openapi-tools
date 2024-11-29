import ballerina/http;

public const RESOURCE_KIND_SERVICE = "Service";
public const RESOURCE_KIND_POD = "Pod";

public type ResourceKind RESOURCE_KIND_SERVICE|RESOURCE_KIND_POD;

public type Metadata record {
    string name;
    string displayName?;
    string description?;
};

public type Status record {
    int observedGeneration;
};

public type ResourceBase record {
    string group;
    string version;
    ResourceKind kind;
    Metadata metadata;
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
    ResourceKind kind = RESOURCE_KIND_SERVICE;
    ServiceSpec spec;
    Status status?;
};

public type PodSpec record {
    string nodeName;
};

public type Pod record {
    *Resource;
    ResourceKind kind = RESOURCE_KIND_POD;
    PodSpec spec;
    Status status?;
};

type RecA record {|
    string a = "a";
    string aa;
|};

type RecB record {|
    *RecA;
    int b;
|};

type RecC record {|
    *RecA;
    string aa = "aa";
    int c;
|};

type RecD record {|
    *RecA;
    string a = "aad";
    int d;
|};

// defaultable `a`` makes requried
type RecE record {|
    *RecA;
    string a;
    string e;
|};

//optional test cases
type RecF record {|
    int f?;
|};

type RecG record {|
    *RecF;
    int g?;
|};

type RecH record {|
    *RecG;
    int f;
    int g;
    string h;
|};

// optional with default value
type RecI record {|
    *RecG;
    int f = 10;
    string i;
    int g;
|};

type RecJ record {|
    *http:Accepted;
    RecK body;
|};

type RecK record {|
    *RecA;
    *RecF;
    *RecL;
    int k;
    string a;
    string first\-name;
|};

type RecL record {|
    string first\-name?;
    int id;
|};

service /payloadV on new http:Listener(7080) {
    resource function get pods() returns Pod[] {
        return [];
    }

    resource function get services() returns Service[] {
        return [];
    }

    resource function get recB() returns RecB[] {
        return [];
    }

    resource function get recC() returns RecC[] {
        return [];
    }

    resource function post recD() returns RecD[] {
        return [];
    }

    resource function post recE() returns RecE[] {
        return [];
    }

    resource function post recH() returns RecH[] {
        return [];
    }

    resource function post recI() returns RecI[] {
        return [];
    }

    resource function post recJ() returns RecJ {
        return {
            body:
            {
                k: 10,
                aa: "abc",
                a: "abcd",
                id: 11,
                first\-name: "lnash"
            }
        };
    }
}
