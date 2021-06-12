#
# + country - Field Description
# + timeline - Field Description
public type VaccineCountryCoverage record {
    string country?;
    SimpleVaccineTimeline|FullVaccineTimeline timeline?;
};

# Covid-19 Vaccine timeline briefly
#
# + date - Field Description
public type SimpleVaccineTimeline record {
    decimal date?;
};

# Descriptive Covid-19 vaccine timeline
#
# + total - Field Description
# + daily - Field Description
# + totalPerHundred - Field Description
# + dailyPerMillion - Field Description
# + date - Field Description
# + fullvaccinetimelinelist - Descriptive Covid-19 vaccine timeline
public type FullVaccineTimeline record {
    record  { decimal total?; decimal daily?; decimal totalPerHundred?; decimal dailyPerMillion?; string date?;} [] fullvaccinetimelinelist;
};
