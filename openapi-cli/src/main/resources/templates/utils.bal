import ballerina/url;

# Generate client request when the media type is given as application/x-www-form-urlencoded.
#
# + encodingMap - Includes the information about the encoding mechanism
# + anyRecord - Record to be serialized
# + return - Serialized request body or query parameter as a string
isolated function createFormURLEncodedRequestBody(any anyRecord, map<[string, boolean]> encodingMap = {}) returns string {
    string[] payload = [];
    if (anyRecord is record {|any|error...; |}) {
        foreach [string, any|error] [key, value] in anyRecord.entries() {
            [string, boolean]|error encodingData;
            string fieldKey = getOriginalKey(key);
            encodingData = trap encodingMap.get(key);
            if (value is string|boolean|int|float) {
                payload[payload.length()] = fieldKey + "=" + getEncodedUri(value.toString());
            } else if (value is string[]|boolean[]|int[]|float[]) {
                if (encodingData is [string, boolean]) {
                    payload[payload.length()] = getSerializedArray(fieldKey, value, encodingData[0], encodingData[1]);
                } else {
                    payload[payload.length()] = getSerializedArray(fieldKey, value);
                }
            } else if (value is record {}) {
                if (encodingData is [string, boolean] && encodingData[0] == "deepObject") {
                    payload[payload.length()] = getDeepObjectStyleRequest(fieldKey, value);
                } else if (encodingData is [string, boolean] && encodingData[0] == "form") {
                    payload[payload.length()] = getFormStyleRequest(fieldKey, value, encodingData[1]);
                }
                else {
                    payload[payload.length()] = getFormStyleRequest(fieldKey, value);
                }
            } else if (value is record {}[]) {
                int count = 0;
                foreach var recordItem in value {
                    if (encodingData is [string, boolean] && encodingData[0] == "deepObject") {
                        payload[payload.length()] = getDeepObjectStyleRequest(fieldKey + "[" + count.toString() + "]", recordItem);
                        payload[payload.length()] = "&";
                    } else if (encodingData is [string, boolean] && encodingData[0] == "form" && encodingData[1] == false) {
                        if (count > 0) {
                            payload[payload.length()] = getFormStyleRequest(fieldKey, recordItem, encodingData[1], true);
                        } else {
                            payload[payload.length()] = getFormStyleRequest(fieldKey, recordItem, encodingData[1]);
                        }
                        payload[payload.length()] = ",";
                    } else {
                        payload[payload.length()] = getFormStyleRequest(fieldKey, recordItem);
                        payload[payload.length()] = "&";
                    }
                    count = count + 1;
                }
                _ = payload.remove(payload.length() - 1);
            }
            payload[payload.length()] = "&";
        }
        _ = payload.remove(payload.length() - 1);
    }
    return string:'join("", ...payload);
}

# Serialize the record according to the deepObject style.
#
# + parent - Parent record name
# + anyRecord - Record to be serialized
# + return - Serialized record as a string
isolated function getDeepObjectStyleRequest(string parent, any anyRecord) returns string {
    string[] recordArray = [];
    if (anyRecord is record {|any|error...; |}) {
        foreach [string, any|error] [key, value] in anyRecord.entries() {
            string fieldKey = getOriginalKey(key);
            if (value is string|boolean|int|float) {
                recordArray[recordArray.length()] = parent + "[" + fieldKey + "]" + "=" + getEncodedUri(value.toString());
            } else if (value is string[]|boolean[]|int[]|float[]) {
                recordArray[recordArray.length()] = getSerializedArray(parent + "[" + fieldKey + "]" + "[]", value, "deepObject", true);
            } else if (value is record {}) { // need to test. Couldnt find any reference for url-encoding nested records
                string nextParent = parent + "[" + fieldKey + "]";
                recordArray[recordArray.length()] = getDeepObjectStyleRequest(nextParent, value);
            } else if (value is record {}[]) {
                string nextParent = parent + "[" + fieldKey + "]";
                int count = 0;
                foreach var recordItem in value {
                    recordArray[recordArray.length()] = getDeepObjectStyleRequest(nextParent + "[" + count.toString() + "]", recordItem);
                    count = count + 1;
                    recordArray[recordArray.length()] = "&";
                }
                _ = recordArray.remove(recordArray.length() - 1);
            }
            recordArray[recordArray.length()] = "&";
        }
        _ = recordArray.remove(recordArray.length() - 1);
    }
    return string:'join("", ...recordArray);
}

# Serialize the record according to the form style.
#
# + parent - Parent record name
# + anyRecord - Record to be serialized
# + explode - Specifies whether arrays and objects should generate separate parameters
# + isNested - Whether the record is inside another record
# + return - Serialized record as a string
isolated function getFormStyleRequest(string parent, any anyRecord, boolean explode = true, boolean isNested = false) returns string {
    string[] recordArray = [];
    if (anyRecord is record {|any|error...; |}) {
        if (explode) {
            foreach [string, any|error] [key, value] in anyRecord.entries() {
                string fieldKey = getOriginalKey(key);
                if (value is string|boolean|int|float) {
                    recordArray[recordArray.length()] = fieldKey + "=" + getEncodedUri(value.toString());
                } else if (value is string[]|boolean[]|int[]|float[]) {
                    recordArray[recordArray.length()] = getSerializedArray(fieldKey, value, explode = explode);
                } else if (value is record {}) { // need to test. Couldnt find any reference for url-encoding nested records
                    recordArray[recordArray.length()] = getFormStyleRequest(parent, value, explode);
                }
                recordArray[recordArray.length()] = "&";
            }
            _ = recordArray.remove(recordArray.length() - 1);
        } else {
            if (!isNested) {
                recordArray[recordArray.length()] = parent;
                recordArray[recordArray.length()] = "=";
            }
            foreach [string, any|error] [key, value] in anyRecord.entries() {
                string fieldKey = getOriginalKey(key);
                if (value is string|boolean|int|float) {
                    recordArray[recordArray.length()] = fieldKey + "," + getEncodedUri(value.toString());
                } else if (value is string[]|boolean[]|int[]|float[]) {
                    recordArray[recordArray.length()] = getSerializedArray(fieldKey, value, explode = false);
                } else if (value is record {}) { // need to test. Couldnt find any reference for url-encoding nested records
                    recordArray[recordArray.length()] = getFormStyleRequest(parent, value, explode, true);
                } //need to implement record array. Couldnt find any reference
                recordArray[recordArray.length()] = ",";
            }
            _ = recordArray.remove(recordArray.length() - 1);
        }
    }
    return string:'join("", ...recordArray);
}

# Serialize arrays.
#
# + arrayName - Name of the field with arrays
# + anyArray - Array to be serialized
# + style - Defines how multiple values are delimited
# + explode - Specifies whether arrays and objects should generate separate parameters
# + return - Serialized array as a string
isolated function getSerializedArray(string arrayName, anydata[] anyArray, string style = "form", boolean explode = true) returns string {
    string key;
    string[] arrayValues = [];
    if string:startsWith(arrayName, "'") {
        key = string:substring(arrayName, 1, arrayName.length());
    } else {
        key = arrayName;
    }
    if (style.equalsIgnoreCaseAscii("form") && !explode) {
        arrayValues[arrayValues.length()] = key;
        arrayValues[arrayValues.length()] = "=";
        foreach anydata i in anyArray {
            arrayValues[arrayValues.length()] = getEncodedUri(i.toString());
            arrayValues[arrayValues.length()] = ",";
        }
        if arrayValues.length() != 0 {
            _ = arrayValues.remove(arrayValues.length() - 1);
        }
    } else if (style.equalsIgnoreCaseAscii("spaceDelimited") && !explode) {
        arrayValues[arrayValues.length()] = key;
        arrayValues[arrayValues.length()] = "=";
        foreach anydata i in anyArray {
            arrayValues[arrayValues.length()] = getEncodedUri(i.toString());
            arrayValues[arrayValues.length()] = "%20";
        }
        if arrayValues.length() != 0 {
            _ = arrayValues.remove(arrayValues.length() - 1);
        }
    } else if (style.equalsIgnoreCaseAscii("pipeDelimited") && !explode) {
        arrayValues[arrayValues.length()] = key;
        arrayValues[arrayValues.length()] = "=";
        foreach anydata i in anyArray {
            arrayValues[arrayValues.length()] = getEncodedUri(i.toString());
            arrayValues[arrayValues.length()] = "|";
        }
        if arrayValues.length() != 0 {
            _ = arrayValues.remove(arrayValues.length() - 1);
        }
    } else if (style.equalsIgnoreCaseAscii("deepObject")){
        foreach anydata i in anyArray {
            arrayValues[arrayValues.length()] = key + "[]";
            arrayValues[arrayValues.length()] = "=";
            arrayValues[arrayValues.length()] = getEncodedUri(i.toString());
            arrayValues[arrayValues.length()] = "&";
        }
        if arrayValues.length() != 0 {
            _ = arrayValues.remove(arrayValues.length() - 1);
        }
    } else {
        foreach anydata i in anyArray {
            arrayValues[arrayValues.length()] = key;
            arrayValues[arrayValues.length()] = "=";
            arrayValues[arrayValues.length()] = getEncodedUri(i.toString());
            arrayValues[arrayValues.length()] = "&";
        }
        if arrayValues.length() != 0 {
            _ = arrayValues.remove(arrayValues.length() - 1);
        }
    }

    return string:'join("", ...arrayValues);
}


# Get Encoded URI for a given value.
#
# + value - Value to be encoded
# + return - Encoded string
isolated function getEncodedUri(string value) returns string {
    string|error encoded = url:encode(value, "UTF8");
    if (encoded is string) {
        return encoded;
    } else {
        return value;
    }
}

# Apostrophe has been added to the keys that could find as ballerina keywords.
# This function will remove the apostrophe from the key.
#
# + keyName - Key to be altered
# + return - Original key
isolated function getOriginalKey(string keyName) returns string {
    if string:startsWith(keyName, "'") {
        return string:substring(keyName, 1, keyName.length());
    } else {
        return keyName;
    }
}

# Generate query path with query parameter.
#
# + queryParam - Query parameter map
# + encodingMap - Details on serialization mechanism
# + return - Returns generated Path or error at failure of client initialization
isolated function getPathForQueryParam(map<anydata> queryParam, map<[string, boolean]> encodingMap = {}) returns string|error {
    string[] param = [];
    param[param.length()] = "?";
    foreach var [key, value] in queryParam.entries() {
        if value is () {
            _ = queryParam.remove(key);
        } else {
            string finalKey;
            if string:startsWith(key, "'") {
                finalKey = string:substring(key, 1, key.length());
            } else {
                finalKey = key;
            }
            if (value is string|boolean|int|float) {
                param[param.length()] = finalKey;
                param[param.length()] = "=";
                param[param.length()] = getEncodedUri(value.toString());
            } else if (value is string[]|boolean[]|int[]|float[]) {
                [string, boolean]|error encodingData = trap encodingMap.get(key);
                if (encodingData is [string, boolean]) {
                    param[param.length()] = getSerializedArray(finalKey, value, encodingData[0], encodingData[1]);
                } else {
                    param[param.length()] = getSerializedArray(finalKey, value);
                }
            } else if (value is record {|any|error...; |}) {
                [string, boolean]|error encodingData = trap encodingMap.get(key);
                if (encodingData is [string, boolean]) {
                    if (encodingData[0] == "deepObject") {
                        param[param.length()] = getDeepObjectStyleRequest(key, value);
                    } else {
                        param[param.length()] = getFormStyleRequest(key, value, encodingData[1]);
                    }
                } else {
                    param[param.length()] = getFormStyleRequest(key, value);
                }
            } else {
                param[param.length()] = finalKey;
                param[param.length()] = "=";
                param[param.length()] = value.toString();
            }
            param[param.length()] = "&";
        }
    }
    _ = param.remove(param.length() - 1);
    if param.length() == 1 {
        _ = param.remove(0);
    }
    string restOfPath = string:'join("", ...param);
    return restOfPath;
}

# Generate header map for given header values.
#
# + headerParam - Headers  map
# + return - Returns generated map or error at failure of client initialization
isolated function getMapForHeaders(map<any> headerParam) returns map<string|string[]> {
    map<string|string[]> headerMap = {};
    foreach var [key, value] in headerParam.entries() {
        if value is string || value is string[] {
            headerMap[key] = value;
        }
    }
    return headerMap;
}

