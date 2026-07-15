import os
import json
import shutil

def should_delete_entry(entry):
    if not isinstance(entry, dict):
        return False

    # A. Remove nested loot tables pointing to the "mt:" namespace
    if entry.get("type") == "minecraft:loot_table":
        val = entry.get("value", "")
        if isinstance(val, str) and val.startswith("mt:"):
            return True

    # B. Remove items that have custom 'mt' framework data or tags
    if "functions" in entry and isinstance(entry["functions"], list):
        for func in entry["functions"]:
            # Check modern 1.20.5+ set_components
            if func.get("function") == "minecraft:set_components":
                components = func.get("components", {})
                custom_data = components.get("minecraft:custom_data", {})
                if isinstance(custom_data, dict):
                    # Drops entry if it contains 'mt' tags or old custom weapons
                    if "mt" in custom_data or "weapon" in custom_data:
                        return True

            # Check legacy set_nbt / set_custom_data
            if func.get("function") in ["minecraft:set_nbt", "minecraft:set_custom_data"]:
                func_str = str(func).lower()
                if '"mt"' in func_str or "totemdeath" in func_str:
                    return True

    return False

def clean_json(data):
    if isinstance(data, dict):
        # Convert Binomial Rolls to static 1
        if "rolls" in data and isinstance(data["rolls"], dict):
            if data["rolls"].get("type") == "minecraft:binomial":
                data["rolls"] = 1

        # Remove redundant weight: 1
        if data.get("weight") == 1:
            data.pop("weight")

        # Fix set_name namespace
        if data.get("function") == "set_name":
            data["function"] = "minecraft:set_name"

        # Remove redundant "set_count" of 1
        if "functions" in data and isinstance(data["functions"], list):
            new_functions = []
            for func in data["functions"]:
                if func.get("function") == "minecraft:set_count":
                    count = func.get("count", {})
                    if isinstance(count, dict) and count.get("min") == 1 and count.get("max") == 1:
                        continue
                    if count == 1:
                        continue
                new_functions.append(clean_json(func))

            if not new_functions:
                data.pop("functions")
            else:
                data["functions"] = new_functions

        # Filter out scoreboard-based conditions
        if "conditions" in data and isinstance(data["conditions"], list):
            new_conditions = []
            for cond in data["conditions"]:
                if cond.get("condition") == "minecraft:entity_scores":
                    scores = cond.get("scores", {})
                    if "mt.total" in scores:
                        continue
                if cond.get("condition") == "minecraft:value_check":
                    val = cond.get("value", {})
                    target = val.get("target", {})
                    if isinstance(target, dict) and target.get("name") == "$vanilla_mode":
                        continue
                new_conditions.append(clean_json(cond))

            if not new_conditions:
                data.pop("conditions")
            else:
                data["conditions"] = new_conditions

        # Filter entries list directly (Drop unwanted items & 'mt:' loot tables)
        if "entries" in data and isinstance(data["entries"], list):
            new_entries = []
            for entry in data["entries"]:
                if should_delete_entry(entry):
                    continue  # Drops the entry completely
                new_entries.append(clean_json(entry))
            data["entries"] = new_entries

        # Recursively clean remaining dictionary keys
        for key in list(data.keys()):
            if key not in ["rolls", "weight", "conditions", "functions", "entries"]:
                data[key] = clean_json(data[key])

    elif isinstance(data, list):
        return [clean_json(item) for item in data]

    return data

def process_directory(directory_path):

    print("\n⚡ Phase 2: Debloating remaining JSON structures...")
    for root, dirs, files in os.walk(directory_path):
        for file in files:
            if file.endswith(".json") and file != "debloat.py":
                file_path = os.path.join(root, file)
                try:
                    with open(file_path, "r", encoding="utf-8") as f:
                        data = json.load(f)

                    cleaned_data = clean_json(data)

                    # If a loot pool ended up with 0 entries after stripping out mt files,
                    # we clean it up so Minecraft doesn't throw a parsing error.
                    if "pools" in cleaned_data and isinstance(cleaned_data["pools"], list):
                        cleaned_data["pools"] = [
                            pool for pool in cleaned_data["pools"]
                            if pool.get("entries")
                        ]

                    with open(file_path, "w", encoding="utf-8") as f:
                        json.dump(cleaned_data, f, indent=2)
                    print(f"  ✅ Cleaned: {file}")
                except Exception as e:
                    print(f"  ⚠️ Error processing {file}: {e}")

if __name__ == "__main__":
    process_directory(".")
    print("\n🎉 Debloating complete! All trace code, files, and links are gone.")