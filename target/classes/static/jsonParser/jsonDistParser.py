import json

with open("../cb_2017_us_cd115_500k.json") as input_district:
    data = json.load(input_district)

# single_data = data["features"][0]
# second_data = data["features"][1]

with open("../usa_states_500k.json") as input_states:
    states_data = json.load(input_states)

# # FOR EVERY STATE
# state_to_districts = {} #dictionary where state : district array
#
# for state in states_data["features"]:
#     this_districts = []
#     for district in data["features"]:
#         if district["properties"]["STATEFP"] == state["properties"]["NAME"]:
#             this_districts.append(district)
#     state_to_districts[state["properties"]["NAME"]] = this_districts


#maryland districts
maryland_districts = []
for state in states_data["features"]:
    if state["properties"]["NAME"] == "Maryland":
        for district in data["features"]:
            if district["properties"]["STATEFP"] == state["properties"]["STATE"]:
                maryland_districts.append(district)

print(maryland_districts)

with open("maryland_districts.json", "w") as outfile:
    json.dump(maryland_districts, outfile)



#kansas districts
kansas_districts = []
for state in states_data["features"]:
    if state["properties"]["NAME"] == "Kansas":
        for district in data["features"]:
            if district["properties"]["STATEFP"] == state["properties"]["STATE"]:
                kansas_districts.append(district)

print(kansas_districts)

with open("kansas_districts.json", "w") as outfile:
    json.dump(kansas_districts, outfile)


#Connecticut Districts
connecticut_districts = []
for state in states_data["features"]:
    if state["properties"]["NAME"] == "Connecticut":
        for district in data["features"]:
            if district["properties"]["STATEFP"] == state["properties"]["STATE"]:
                connecticut_districts.append(district)

print(connecticut_districts)

with open("connecticut_districts.json", "w") as outfile:
    json.dump(connecticut_districts, outfile)



#
# for state in states_data["features"]:
#     if state["NAME"] == "Kansas":
#
#
# for state in states_data["features"]:
#     if state["NAME"] == "Connecticut":

