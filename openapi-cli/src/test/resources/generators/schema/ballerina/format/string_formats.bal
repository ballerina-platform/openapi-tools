# formats those are defined by the OpenAPI Specification
public type OASStringFormats record {
    string name?;
    string byteContent?;
    record {byte[] fileContent; string fileName;} binaryContent?;
    string dateContent?;
    string passwordContent?;
    string datetimeContent?;
};

# formats those are not defined by the OpenAPI Specification
public type NONOASStringFormats record {
    string uuidContent?;
    string uriContent?;
    string emailContent?;
    string hostnameContent?;
    string ipv4Content?;
    string ipv6Content?;
};
