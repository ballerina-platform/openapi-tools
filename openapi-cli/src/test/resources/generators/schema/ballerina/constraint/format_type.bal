import ballerina/constraint;

public type IntegerObject record {
    string name?;
    @constraint:Int {maxValue: 2147483647}
    int:Signed32 int32Content?;
    @constraint:Int {minValue: 0}
    int int64Content?;
};

public type StringObject record {
    string name?;
    @constraint:String {pattern: re `^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$`}
    string byteContent?;
    record {byte[] fileContent; string fileName;} binaryContent?;
    @constraint:String {pattern: re `^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$`}
    string uuidContent?;
    @constraint:String {pattern: re `^[a-zA-Z][a-zA-Z0-9+.-]*:[a-zA-Z0-9+.-]+$`}
    string uriContent?;
    @constraint:String {pattern: re `^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$`}
    string dateContent?;
    @constraint:String {pattern: re `^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$`}
    string passwordContent?;
    @constraint:String {pattern: re `^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$`}
    string datetimeContent?;
    @constraint:String {pattern: re `^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$`}
    string emailContent?;
    @constraint:String {pattern: re `^[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9](\.[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9])*$`}
    string hostnameContent?;
    @constraint:String {pattern: re `^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$`}
    string ipv4Content?;
    @constraint:String {pattern: re `^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$`}
    string ipv6Content?;
};

public type NumberObject record {
    string name?;
    @constraint:Float {minValue: 0.1}
    float floatContent?;
    @constraint:Number {maxValue: 200}
    decimal doubleContent?;
};
