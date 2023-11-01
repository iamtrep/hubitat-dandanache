# Knockturn Alley

Simple toolkit driver to help developers peer deep into the guts of Zigbee devices.

**Note:** This driver is useless to non-developers as it cannot actually control any smart device.

| Spells | Attributes Report |
|--------|-------------------|
| ![Spells](img/Screenshot_1.png) | ![Attributes Report](img/Screenshot_2.png) |

> Skulking around Knockturn Alley? Dodgy place. Don't want no one to see you there. \
> -- Hagrid

## Usage
This driver has no registered fingerprints and no configuration / initialization procedure so it does not support the pairing process for new devices. Regular hacking workflow goes like this:

1. If you have a new Zigbee device, pair it as usual with the correct driver. For already paired devices, there's nothing to do here.
2. If you want to know more about a Zigbee device, go to the device details page and change the driver to `Knockturn Alley`.
3. No initialization / configuration is required.
4. Cast whatever spells you want using the `Knockturn Alley` driver. Have the `Logs` section opened in a separate tab since the driver talks to you mostly via log entries.
5. When you decided you had enough fun, cast the `Obliviate` spell with option `1` to get rid of the `ka_*` device state entries (we clean our own mess).
6. Seriously, do step 5. This driver creates multiple state entries, not to mention the generated report, and this will hit your Hubs RAM.
7. From the device details page, change back to the original driver. Everything should work without the need to reconfigure / re-pair the device.
8. Pick a new Zigbee device to torture and go back to Step 2 :)


## Spells

Spells for auto discovering Zigbee features:
- [A01 - Legilimens](#a01---legilimens)
- [A02 - Scourgify](#a02---scourgify)

Spells for handling Zigbee Attributes:
- [B01 - Accio](#b01---accio)
- [B02 - Everte Statum](#b02---everte-statum)
- [B03 - Oppugno](#b03---oppugno)

Spells for executing Zigbee Commands and managing driver state:
- [C01 - Imperio](#c01---imperio)
- [C02 - Obliviate](#c02---obliviate)

Spells for handling Neighbors Table (LQI), Routing Table and Bindings Table:
- [D01 - Revelio](#d01---revelio)
- [D02 - Unbreakable Vow](#d02---unbreakable-vow)


### A01 - Legilimens
<img src="img/Legilimens.gif" height="200px"/>

`Legilimens` spell automatically collects information on all Zigbee attributes that the device exposes. When cast, it will:
1. Retrieve all Zigbee endpoints (e.g.: 0x01 = Default endpoint)
2. For each endpoint, retrieve in and out clusters (e.g.: 0x0006 = On/Off Cluster)
3. For each in cluster, discover attributes (e.g.: 0x0400 = SWBuildID - for cluster 0x0000)
4. For each attribute, ask the device to send its current value
5. If an attribute is known to be reportable, ask the device to send its current reporting configuration

Note: If you specify a manufacturer code (optional), the spell will try to also discover attributes and commands specific to that manufacturer. In the generated report, manufacturer specific attributes and commands are prefixed by `0_` (e.g.: attribute `0_0043`, command `0_01`).

Before casting the spell, have the Logs section open in order to take a peak at the chatty conversation that the driver is having with the device. Be patient, the discovering process will take about 1 minute to finish (depending on the number of endpoints/clusters/attributes). Keep your eyes on the Logs to see when the driver stops adding log entries.

> **Important**: If the device is battery-powered, press any button to wake it before casting the `Legilimens` spell; then, keep on pressing buttons every second or so in order to prevent the device from going back to sleep.

When the discovery process is complete, refresh the device details page to see what data was gathered. This data will be hard to follow in its raw form, so you should continue with casting the next spell.

### A02 - Scourgify
<img src="img/Scourgify.webp" height="200px"/>

`Scourgify` spell cleans up the data mess we got after casting the `Legilimens` spell. When cast, it will:
1. Read data gathered using the `Legilimens` spell. You need to first cast the `Legilimens`, otherwise nothing will happen.
2. Use the raw data to create a friendly attributes report.

After casting the spell, refresh the device details page to see the generated report.

You can Control + Click anywhere on the generated report to select all text, and now you're ready to share it using Copy / Paste.

### B01 - Accio
<img src="img/Accio.gif" height="200px"/>

`Accio` spell retrieves information about the Zigbee attribute identified by the endpoint / cluster / attribute coordinates. When cast, it can:
1. Read the current value of the specified attribute.
2. Read the reporting configuration for the specified attribute.

Before casting the spell, have the Logs section open in order to see the device response.

### B02 - Everte Statum
<img src="img/Everte_Statum.webp" height="200px"/>

`Everte Statum` spell updates the value for the specified Zigbee attribute. You can now fight back and do some real damage to your devices!

After casting the spell, you may want to cast `Accio` to query the device for the updated attribute value.

### B03 - Oppugno
<img src="img/Oppugno.webp" height="200px"/>

`Oppugno` spell configures the reporting details for the specified Zigbee attribute.

Notes:
- If Min Interval is set to 0 (0x0000), then there is no minimum limit, unless one is imposed by the specification of the cluster using this reporting mechanism or by the application
- If Max Interval is set to 65535 (0xFFFF), then the device SHALL not issue reports for the specified attribute, and the configuration information for that attribute need not be maintained (in an implementation using dynamic memory allocation, the memory space for that information may then be reclaimed).
- If Max Interval is set to 0 (0x0000) and Min Interval is set to 65535 (0xFFFF), then the device SHALL revert back to its default reporting configuration. The reportable change field, if present, SHALL be set to zero.

After casting the spell, you may want to cast `Accio` to query the device for the updated reporting configuration for that specific attribute.

### C01 - Imperio
<img src="img/Imperio.gif" height="200px"/>

`Imperio` spell executes the specified Zigbee command. Keep an eye on the Logs section to see if you got the command payload right!

### C02 - Obliviate
<img src="img/Obliviate.gif" height="200px"/>

`Obliviate` spell is used to forget specific information present in the device details page. When cast, it can remove:
1. Our state variables (ka_*) - Remove only information that was added by this driver, so that you can go back to using the original driver.
2. All state variables - Remove all stored state data. You may use this if you want to switch drivers and start with a clean state.
3. Device data - Remove all information present in the `Device Details -> Data` section. Useful when switching drivers.
4. Scheduled jobs configured by the previous driver. Useful when switching drivers.
5. Everything - Forget everything, start anew.

After casting the spell, refresh the device details page to see that the specified information vanished into the void.

### D01 - Revelio
<img src="img/Revelio.webp" height="200px"/>

`Revelio` spell retrieves information about the:
1. Neighbors Table (LQI)
2. Routing Table
3. Bindings Table

Before casting the spell, have the Logs section open in order to see the device response.

### D02 - Unbreakable Vow
<img src="img/Unbreakable_Vow.gif" height="200px"/>

`Unbreakable Vow` spell will help you to add/remove entries to/from the Bindings Table.

After casting the spell, you may want to cast `Revelio` to query the device for the updated Bindings Table.

---
[<img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 40px !important;width: 162px !important">](https://www.buymeacoffee.com/dandanache)
