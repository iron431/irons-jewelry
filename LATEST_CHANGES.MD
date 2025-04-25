### Additions
- Added Dynamic Loot Injections for Jewelry items
  - Dynamically inserts jewelry items into loot tables based on the loot table's contents above some threshold
  - Loot tables with more gems, equipment, or other tagged items have a higher change to generate jewelry
  - Added corresponding item tags: `loot_handler/low_gearscore`,`loot_handler/medium_gearscore`,`loot_handler/high_gearscore`,`loot_handler/very_high_gearscore`
- Added Serverconfig file, with options for disabling loot injections and changing jeweler house weight
- Added Japanese localization, thanks to Mohuzato
- Added Russian localization, thanks to Heimdallr

### Changes
- Adjusted Barbed Band texture

### Fixes
- 

### API
- Now requires Atlas API v1.1.0, and cannot exceed v1.2.0 (exclusive)

