// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

#
# + id - Field Description
# + name - Field Description
# + tag - Field Description
# + 'type - Field Description
public type Pet record {
    int id;
    string name;
    string tag?;
    string 'type?;
};

#
# + bark - Field Description
public type Dog record {
    *Pet;
    boolean bark?;
};

#
# + petslist - Field Description
public type Pets record {
    Pet[] petslist;
};

#
# + code - Field Description
# + message - Field Description
public type Error record {
    int code;
    string message;
};
