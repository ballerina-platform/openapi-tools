import ballerina/websub;

public type SpecialReturnType record {|
    string name;
    string description;
|};

@websub:SubscriberServiceConfig{}
service /sample on new websub:Listener(10009) {
    remote function onEventNotification(websub:ContentDistributionMessage event) {
        return;
    }

    remote function onSubscriptionVerification(websub:SubscriptionVerification msg) {
        return;
    }
}