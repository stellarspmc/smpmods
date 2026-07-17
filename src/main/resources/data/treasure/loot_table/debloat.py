import os
import json

# Define roll rules per rarity tier
ROLL_CONFIGS = {
    "rare": {"min": 2, "max": 5},
    "epic": {"min": 2, "max": 5},
    "legendary": {"min": 3, "max": 4}
}

def get_roll_config(file_path):
    """Determines roll min/max based on file name or path."""
    path_lower = file_path.lower()
    for rarity, config in ROLL_CONFIGS.items():
        if rarity in path_lower:
            return config
    return None

def update_rolls(data, roll_config):
    """Recursively updates 'rolls' fields in the JSON structure."""
    if isinstance(data, dict):
        if "rolls" in data:
            data["rolls"] = roll_config.copy()
        for value in data.values():
            update_rolls(value, roll_config)
    elif isinstance(data, list):
        for item in data:
            update_rolls(item, roll_config)

def process_directory(directory_path="."):
    print("\n⚡ Updating loot table rolls...")

    for root, _, files in os.walk(directory_path):
        for file in files:
            if not file.endswith(".json"):
                continue

            file_path = os.path.join(root, file)
            roll_config = get_roll_config(file_path)

            # Skip files that don't match rare, epic, or legendary
            if not roll_config:
                continue

            try:
                with open(file_path, "r", encoding="utf-8") as f:
                    data = json.load(f)

                update_rolls(data, roll_config)

                with open(file_path, "w", encoding="utf-8") as f:
                    json.dump(data, f, indent=2)

                print(f"  ✅ Updated {file}: rolls set to {roll_config['min']}-{roll_config['max']}")
            except Exception as e:
                print(f"  ⚠️ Error processing {file}: {e}")

if __name__ == "__main__":
    process_directory(".")
    print("\n🎉 Completed roll updates!")