# Hubitat Zigbee Map

Allows you to visually render the Zigbee map of your Hubitat system.

![Zigbee Map](zigbee-map.png "Zigbee Map")

### Install using HPM (offers automatic updates)
Follow the steps below if you already have the "Hubitat Package Manager" app installed in your Hubitat hub. This steps must only be executed once:
   * In the Hubitat interface, click "Apps" in the left menu.
   * Click "Hubitat Package Manager" from the apps list.
   * Click "Install", then "Search by Keywords".
   * Enter "Zigbee Map" in the search box and click "Next".
   * Click "Zigbee Map by Dan Danache" and click "Next".
   * Read the license agreement and click "Next".
   * When the "Installation complete" message appears, click "Next".
   * In the Hubitat interface, click "Apps" in the left menu.
   * Click "Add user app" button in the top right.
   * Click "Zigbee Map" from the list.

To access the Zigbee Map app at any time, follow these steps:
   * In the Hubitat interface, click "Apps" in the left menu.
   * Click "Zigbee Map" from the apps list.
   * Click the "View Zigbee map" option.
   * Sit back and watch the application build the map of your Zigbee mesh.
   * The Zigbee map is complete once the Interview Queue is empty.

### How it works
Each Zigbee device keeps a list of devices that it can communicate best with and that can be used to reach to other devices in the mesh. This list is called the Neighbors Table.

The Zigbee map is built by directly interviewing every Zigbee device in the mesh for its Neighbors Table. That's why the rendered Zigbee map should be very accurate.

Once you start the HTML application, the following happens:
1. First, the application asks the Zigbee Coordinator (Hubitat hub) for its Neighbors Table.
1. Once the Coordinator's neighbors list is retrieved, all neighbors are put in an Interview Queue. This is done in order to make sure that only one device is interviewed at a time.
1. The data gathering process continues with the neighbor's neighbors, and it stops only when all devices are interviewed (Interview Queue is empty).

> **Important**<br>
> Be patient. Each interview require multiple interactions between the application and the device, so getting the full neighbors list for a single Zigbee device might take up-to 20 seconds.

Notes:
- Some Zigbee Router devices ignore the command to provide their Neighbors List. These are crappy devices you should probably bin for not conforming to the Zigbee specification.
- If a device does not respond to the interview request, a timeout is issued after 25 seconds.
- Zigbee End Devices (usually battery powered, sleepy devices) are not interviewed since they won't probably respond to the interview, on the account that they are busy sleeping.
- Some Zigbee End Devices are reported by their neighbors as Zigbee Routers, so the app tries to interview them (just wait for the 25 seconds timeout to kick in).
- Once the Zigbee Map is completely built, you can right-click any node to add it back to the Interview Queue for another round :)


---
[<img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 40px !important;width: 162px !important">](https://www.buymeacoffee.com/dandanache)
