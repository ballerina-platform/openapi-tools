public type VaccineCountryCoverage record {
    string country?;
    SimpleVaccineTimeline|FullVaccineTimeline timeline?;
    };
public type SimpleVaccineTimeline record {
    decimal date?;
};
public type FullVaccineTimeline record {
    record { decimal total?;
             decimal daily?;
             decimal totalPerHundred?;
             decimal dailyPerMillion?;
             string date?
             ;} [] fullvaccinetimelinelist;
};
