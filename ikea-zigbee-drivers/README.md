# IKEA Zigbee drivers
This document provides instructions on how to install and use custom drivers for IKEA Zigbee devices on Hubitat hub. These drivers enable advanced features and functionalities for the following devices:

* [Askvader On/Off Switch (E1836)](#askvader-onoff-switch-e1836)
* [Badring Water Leakage Sensor (E2202)](#badring-water-leakage-sensor-e2202)
* [LED Driver (ICPSHC24)](#led-driver-icpshc24)
* [Parasoll Door/Window Sensor (E2013)](#parasoll-doorwindow-sensor-e2013)
* [Rodret Dimmer (E2201)](#rodret-dimmer-e2201)
* [Somrig Shortcut Button (E2213)](#somrig-shortcut-button-e2213)
* [Starkvind Air Purifier (E2006)](#starkvind-air-purifier-e2006)
* [Styrbar Remote Control N2 (E2002)](#styrbar-remote-control-n2-e2002)
* [Symfonisk Sound Remote Gen2 (E2123)](#symfonisk-sound-remote-gen2-e2123)
* [Tradfri Control Outlet (E1603, E1706)](#tradfri-control-outlet-e1603-e1706)
* [Tradfri Motion Sensor (E1745)](#tradfri-motion-sensor-e1745)
* [Tradfri On/Off Switch (E1743)](#tradfri-onoff-switch-e1743)
* [Tradfri Open/Close Remote (E1766)](#tradfri-openclose-remote-e1766)
* [Tradfri Remote Control (E1810)](#tradfri-remote-control-e1810)
* [Tradfri Shortcut Button (E1812)](#tradfri-shortcut-button-e1812)
* [Vallhorn Motion Sensor (E2134)](#vallhorn-motion-sensor-e2134)
* [Vindstyrka Air Quality Sensor (E2112)](#vindstyrka-air-quality-sensor-e2112)

## Installation
There are two ways to install the drivers: using Hubitat Package Manager (HPM) or manually importing the driver code.

### HPM Installation (Recommended)
HPM is an app that allows you to easily install and update custom drivers and apps on your Hubitat hub. To use HPM, you need to have it installed on your hub first.

Once you have HPM installed, follow these steps to install the IKEA Zigbee drivers:

- In the Hubitat interface, go to **Apps** and select **Hubitat Package Manager**.
- Select **Install**, then **Search by Keywords**.
- Enter **IKEA Zigbee drivers** in the search box and click **Next**.
- Select **IKEA Zigbee drivers by Dan Danache** and click **Next**.
- Select the driver(s) you need from the dropdown list and follow the install instructions.

### Manual Installation
If you don’t want to use HPM, you can also install the drivers manually by importing the driver code from GitHub. Follow these steps to do so:

- In the Hubitat interface, go to **Drivers Code**.
- Click **New Driver** in the top right, then click **Import** in the top right.
- Search below for your device, look for the **Manual install file** property and enter it in the URL field.
- Click **Import**, then click **OK** and the code should load in the editor.
- Click **Save** in the top right.

More info about installing custom drivers is available in the [Official Documentation](https://docs2.hubitat.com/en/how-to/install-custom-drivers).

## Device Features and Pairing
Below you can find the details of each device, including the features, parameters, and pairing instructions. Click on the device name to jump to the corresponding section.


### Askvader On/Off Switch (E1836)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_E1836.webp" style="width: 200px"> |
| Product Code | `504.638.80` |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E1836.groovy` |
| Tested firmwares | `1.0.002` |
| Since version | `3.1.0` |

#### Features
* Commands: On, Off, Toggle, On with Timed Off
* Configure what happens after a power outage (Power On, Power Off, Restore previous state)
* Health status: online / offline
* Refresh switch state on demand

#### Device Pairing
1. Locate the small reset hole (located between the On/Off button and the LED light) and have a pin or pencil ready to fit the hole.
1. If the device is already plugged in, unplug it for 20 seconds (power-cycle) before each pairing attempt.
1. Plug the device back into an outlet.
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. Insert the pin into the reset hole and press it for at least 5 seconds; the LED light will start blinking upon release.
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. You're all set! Enjoy using your Askvader On/Off Switch.


### Badring Water Leakage Sensor (E2202)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_E2202.webp" style="width: 200px"> |
| Product Code | `605.043.52` |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E2202.groovy` |
| Tested firmwares | `1.0.7` |
| Since version | `3.9.0` |

#### Features
* Water detection: wet / dry
* Battery indicator: %
* Health status: online / offline

#### Device Pairing
1. Open the battery compartiment and you should see the small pair button (🔗).
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. **Important**: Move closer to your Hubitat hub and press the pair button in the battery compartment **four times within five seconds**.
1. **Important**: Immediately after the device LED starts blinking red, position the device as close as possible to your Hubitat hub for **at least 30 seconds** (wait until the LED stops blinking and turns off).
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. Close the device battery compartiment.
1. You're all set! Enjoy using your Badring Water Leakage Sensor.

### LED Driver (ICPSHC24)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_ICPSHC24.webp" style="width: 200px"> |
| Product Code | `603.426.56`｜`503.561.87`｜`104.747.72` |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/ICPSHC24.groovy` |
| Tested firmwares | 10EU-IL-1: `1.2.245` |
|| 30EU-IL-2: `1.0.002 |
|| 30-IL44-1: `1.0.021` (Silverglans) |
| Since version | `3.1.0` |

#### Features
1. Commands: On, Off, Toggle, On with Timed Off
1. Configure what happens after a power outage (Power On, Power Off, Restore previous state)
1. Brightness control: Set brightness level, Start/Stop level change, Level up/down
1. Configure brightness level when turned on (Always the same value, Restore last level)
1. Can set the brightness level when the lights are off (and they stay off). When the lights are turned on, they will start at the specified level.
1. Health status: online / offline
1. Refresh switch state on demand

#### Known Issues
* Smaller (10W) drivers do not honor the Power On Behavior.

#### Device Pairing
1. Have a light attached to the LED Driver device.
1. Find the small reset hole on the device and make sure you have at hand a pin that can fit the reset hole (e.g.: a paper clip or SIM card eject pin).
1. If the device is already plugged in, unplug it for 20 seconds (power-cycle) before each pairing attempt.
1. Plug the device back into an outlet.
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. Insert the pin into the reset hole and press it for at least 5 seconds; the attached light will blink then stay on.
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. You're all set! Enjoy using your LED Driver.


### Parasoll Door/Window Sensor (E2013)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_E2013.webp" style="width: 200px"> |
| Product Code | `805.043.08` |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E2013.groovy` |
| Tested firmwares | `1.0.19` |
| Since version | `3.6.0` |

#### Features
* Contact status: open / close
* Battery indicator: %
* Health status: online / offline

#### Device Pairing
1. Remove device from its position using a small screwdriver and, on the back, you should see the small pair button (🔗).
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. **Important**: Move closer to your Hubitat hub and press the pair button in the battery compartment **four times within five seconds**.
1. **Important**: Immediately after the device LED starts blinking red, position the device as close as possible to your Hubitat hub for **at least 30 seconds** (wait until the LED stops blinking and turns off).
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. Place the device back to its position by gently pressing it until you hear it clicking in place.
1. You're all set! Enjoy using your Parasoll Door/Window Sensor.


### Rodret Dimmer (E2201)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_E2201.webp" style="width: 200px"> |
| Product Code | `205.281.28`｜`805.597.96` |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E2201.groovy` |
| Tested firmwares | `1.0.47` |
| Since version | `2.0.0` |

#### Features
* Button Push events: both buttons
* Button Hold events: both buttons
* Button Release events: both buttons
* Battery indicator: %
* Health status: online / offline

#### Device Pairing
1. Open the battery compartiment and you should see the small pair button (🔗).
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. **Important**: Move closer to your Hubitat hub and press the pair button in the battery compartment **four times within five seconds**.
1. **Important**: Immediately after the device LED starts blinking red, position the device as close as possible to your Hubitat hub for **at least 30 seconds** (wait until the LED stops blinking and turns off).
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. Close the device battery compartiment.
1. You're all set! Enjoy using your Rodret Dimmer.


### Somrig Shortcut Button (E2213)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_E2213.webp" style="width: 200px"> |
| Product Code | `305.603.54` |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E2213.groovy` |
| Tested firmwares | `1.0.20` |
| Since version | `3.6.0` |

#### Features
* Button Push events: both buttons
* Button Double-Tap event: both buttons
* Button Hold events: both buttons
* Button Release events: both buttons
* Battery indicator: %
* Health status: online / offline

#### Device Pairing
1. Using a small screwdriver, open the battery compartiment and you should see the small pair button (🔗).
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. **Important**: Move closer to your Hubitat hub and press the pair button in the battery compartment **four times within five seconds**.
1. **Important**: Immediately after the device LED starts blinking red, position the device as close as possible to your Hubitat hub for **at least 30 seconds** (wait until the LED stops blinking and turns off).
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. Close the device battery compartiment.
1. You're all set! Enjoy using your Somrig Shortcut Button.


### Starkvind Air Purifier (E2006)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_E2006.webp" style="width: 200px"> |
| Product Code | `194.442.19` |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E2006.groovy` |
| Tested firmwares | `1.0.033`｜`1.1.001` |
| Since version | `3.5.0` |

#### Features
* Commands: On, Off, Toggle, Set/Cycle fan speed
* Particulate Matter < 2.5 microns (PM 2.5) sensor data
* Calculate US AQI score based on PM 2.5 value
* Filter usage (%) and Filter status (normal / replace) information
* Configuration: Sensor report frequency, Filter life time, Child lock, LED status
* Health status: online / offline
* Refresh device state on demand

#### Device Pairing
1. If the device is already plugged in, unplug it for 20 seconds (power-cycle) before each pairing attempt.
1. Plug the device back into an outlet.
1. Open the round top lid and you should see the pair button (🔗) next to the filter reset button.
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. Press and hold the pair button **for at least 5 seconds** until the LED starts blinking.
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. Put back the lid and fix it in place.
1. You're all set! Enjoy using your Starkvind Air Purifier.


### Styrbar Remote Control N2 (E2002)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_E2002.webp" style="width: 200px"> |
| Product Code | `304.883.63` |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E2002.groovy` |
| Tested firmwares | `1.0.024`｜`2.4.5` |
| Since version | `2.0.0` |

#### Features
* Button Push events for: all buttons
* Button Hold events for: Button 1 (🔆) and Button 2 (🔅)
* Button Release events for: Button 1 (🔆), Button 2 (🔅)
* Battery indicator: %
* Health status: online / offline

#### Known Issues
* The Hold / Release events don't work correctly on the Next and Prev buttons.
* Old firmware versions (below 2.4.5) send multiple events per button push. You can update the device to the latest version using Hubitat.

#### Device Pairing
1. Using a small screwdriver, open the battery compartiment and you should see the small pair button (🔗).
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. **Important**: Move closer to your Hubitat hub and press the pair button in the battery compartment **four times within five seconds**.
1. **Important**: Immediately after the device LED starts blinking red, position the device as close as possible to your Hubitat hub for **at least 30 seconds** (wait until the LED stops blinking and turns off).
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. Close the device battery compartiment.
1. You're all set! Enjoy using your Styrbar Remote Control.


### Symfonisk Sound Remote Gen2 (E2123)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_E2123.webp" style="width: 200px"> |
| Product Code | `305.273.12` |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E2123.groovy` |
| Tested firmwares | `1.0.012`｜`1.0.35` |
| Since version | `1.0.0` |

#### Features
* Button Push events: all buttons
* Button Hold events: Button 2 (Plus), Button 3 (Minus), Button 6 (•) and Button 7 (••)
* Button Release events: Button 6 (•) and Button 7 (••)
* Button Double-Tap events: Button 6 (•) and Button 7 (••)
* Battery indicator: %
* Health status: online / offline

#### Device Pairing
1. Open the battery compartiment and you should see the small pair button (🔗).
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. **Important**: Move closer to your Hubitat hub and press the pair button in the battery compartment **four times within five seconds**.
1. **Important**: Immediately after the device LED starts blinking red, keep the device **as close as you can** against your Hubitat hub until the LED stops blinking and turns off.
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. Close the device battery compartiment.
1. You're all set! Enjoy using your Symfonisk Sound Remote.


### Tradfri Control Outlet (E1603, E1706)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_E1603.webp" style="width: 200px"> |
| Product Code | `304.883.63`｜`303.561.69` |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E1603.groovy` |
| Tested firmwares | `2.0.0244`｜`2.3.089` |
| Since version | `2.0.0` |

#### Features
* Commands: On, Off, Toggle, On with Timed Off
* Configure what happens after a power outage (Power On, Power Off, Restore previous state)
* Health status: online / offline
* Refresh switch state on demand

#### Device Pairing
1. Find the small reset hole on the side of the device and make sure you have at hand a pin that can fit the reset hole
   (e.g.: a paper clip or SIM card eject pin).
1. If the device is already plugged in, unplug it for 20 seconds (power-cycle) before each pairing attempt.
1. Plug the device back into an outlet.
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. Insert the pin into the reset hole and press it for at least 5 seconds; upon release, the LED light will start
   blinking.
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. You're all set! Enjoy using your Tradfri Control Outlet.


### Tradfri Motion Sensor (E1745)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_E1745.webp" style="width: 200px"> |
| Product Code | `704.299.13` |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E1745.groovy` |
| Tested firmwares | `24.4.5` |
| Since version | `2.3.0` |

#### Features
* Motion detection: active / inactive (50 seconds cooldown)
* Illumination detection: dim / bright
* Option to detect motion only when dark
* Battery indicator: %
* Health status: online / offline

#### Known Issues
* Old firmware versions (below 24.4.5) suppport binding to groups only and this functionality is not supported by the
  Hubitat hub. You can update the device to the latest version using Hubitat.
* Illumination is reported only when motion is detected.

#### Device Pairing
1. Open the back compartiment and you should see the small pair button (🔗).
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. **Important**: Move closer to your Hubitat hub and press the pair button in the battery compartment **four times within five seconds**.
1. **Important**: Immediately after the device LED starts blinking red, position the device as close as possible to your Hubitat hub for **at least 30 seconds** (wait until the LED stops blinking and turns off).
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. Close the device battery compartiment.
1. You're all set! Enjoy using your Tradfri Motion Sensor.


### Tradfri On/Off Switch (E1743)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_E1743.webp" style="width: 200px"> |
| Product Code | `203.563.82`｜`404.677.65`｜`403.563.81` |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E1743.groovy` |
| Tested firmwares | `2.2.010`｜`24.4.6` |
| Since version | `2.0.0` |

#### Features
* Button Push events: both buttons
* Button Hold events: both buttons
* Button Release events: both buttons
* Battery indicator: %
* Health status: online / offline

#### Device Pairing
1. Using a small screwdriver, open the battery compartiment and you should see the small pair button (🔗).
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. **Important**: Move closer to your Hubitat hub and press the pair button in the battery compartment **four times within five seconds**.
1. **Important**: Immediately after the device LED starts blinking red, position the device as close as possible to your Hubitat hub for **at least 30 seconds** (wait until the LED stops blinking and turns off).
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. Close the device battery compartiment.
1. You're all set! Enjoy using your Tradfri On/Off Switch.


### Tradfri Open/Close Remote (E1766)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_E1766.webp" style="width: 200px"> |
| Product Code | |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E1766.groovy` |
| Tested firmwares | `24.4.6` |
| Since version | `3.3.0` |

#### Features
* Button Push events: both buttons
* Button Release events: both buttons
* Battery indicator: %
* Health status: online / offline

#### Device Pairing
1. Using a small screwdriver, open the battery compartiment and you should see the small pair button (🔗).
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. **Important**: Move closer to your Hubitat hub and press the pair button in the battery compartment **four times within five seconds**.
1. **Important**: Immediately after the device LED starts blinking red, position the device as close as possible to your Hubitat hub for **at least 30 seconds** (wait until the LED stops blinking and turns off).
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. Close the device battery compartiment.
1. You're all set! Enjoy using your Tradfri Open/Close Remote.


### Tradfri Remote Control (E1810)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_E1810.webp" style="width: 200px"> |
| Product Code | `304.431.24`｜`004.431.30` |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E1810.groovy` |
| Tested firmwares | `24.4.5` |
| Since version | `1.1.0` |

#### Features
* Button Push events: all buttons
* Button Hold events: Button 2 (🔆), Button 3 (🔅), Button 4 (Next) and Button 5 (Prev)
* Button Release events: Button 2 (🔆), Button 3 (🔅), Button 4 (Next) and Button 5 (Prev)
* Button 1 (Play) acts as a switch (on / off)
* Button 2 (🔆) and Button 3 (🔅) act as a switch level (0 - 100%)
* Battery indicator: %
* Health status: online / offline

#### Known Issues
* Old firmware versions suppport binding to groups only and this functionality is not supported by the Hubitat hub. You can update the device to the latest version using Hubitat.

#### Device Pairing
1. Open the battery compartiment and you should see the small pair button (🔗).
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. **Important**: Move closer to your Hubitat hub and press the pair button in the battery compartment **four times within five seconds**.
1. **Important**: Immediately after the device LED starts blinking red, position the device as close as possible to your Hubitat hub for **at least 30 seconds** (wait until the LED stops blinking and turns off).
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. Close the device battery compartiment.
1. You're all set! Enjoy using your Tradfri Remote Control.


### Tradfri Shortcut Button (E1812)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_E1812.webp" style="width: 200px"> |
| Product Code | `203.563.82`｜`404.677.65`｜`403.563.81` |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E1812.groovy` |
| Tested firmwares | `2.3.015`｜`24.4.6` |
| Since version | `2.0.0` |

#### Features
* Button Push event
* Button Double-Tap event (only on firmware `24.4.6` and above)
* Button Hold event
* Button Release event
* Battery indicator: %
* Health status: online / offline

#### Device Pairing
1. Using a small screwdriver, open the battery compartiment and you should see the small pair button (🔗).
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. **Important**: Move closer to your Hubitat hub and press the pair button in the battery compartment **four times within five seconds**; the red LED light can be seen from the back / battery side.
1. **Important**: Immediately after the device LED starts blinking red, position the device as close as possible to your Hubitat hub for **at least 30 seconds** (wait until the LED stops blinking and turns off).
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. Close the device battery compartiment.
1. You're all set! Enjoy using your Tradfri Shortcut Button.


### Vallhorn Motion Sensor (E2134)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_E2134.webp" style="width: 200px"> |
| Product Code | `405.043.48` |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E2134.groovy` |
| Tested firmwares | ` 1.0.57` |
| Since version | `3.6.0` |

#### Features
* Motion detection: active / inactive (20 seconds cooldown)
* Illumination lux reporting (5 minutes cooldown)
* Battery indicator: %
* Health status: online / offline

#### Known Issues
* Max reported illuminance is 1364 lux; more than enough for indoor usage

#### Device Pairing
1. Remove device from its position using a small screwdriver and, on the back, you should see the small pair button (🔗).
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. **Important**: Move closer to your Hubitat hub and press the pair button in the battery compartment **four times within five seconds**.
1. **Important**: Immediately after the device LED starts blinking red, keep the device **as close as you can** against your Hubitat hub for ****at least 30 seconds**** (after the front light stops blinking and turns off).
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. Place the device back to its position by gently pressing it until you hear it clicking in place.
1. You're all set! Enjoy using your Vallhorn Motion Sensor.


### Vindstyrka Air Quality Sensor (E2112)

| Parameter | Details |
|-----------|-------------|
| Product Image | <img src="img/Ikea_E2112.webp" style="width: 200px"> |
| Product Code | `404.982.34` |
| Manual install file | `https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E2112.groovy` |
| Tested firmwares | `1.0.10`｜`1.0.11` |
| Since version | `3.8.0` |

#### Features
* Particulate Matter < 2.5 microns (PM 2.5) sensor data
* Calculate US AQI score based on PM 2.5 value
* [Sensirion VOC Index](https://sensirion.com/media/documents/02232963/6294E043/Info_Note_VOC_Index.pdf) (1-500)
* Temperature and Relative Humidity
* Health status: online / offline

#### Device Pairing
1. In the Hubitat interface, navigate to **Devices**, click **Add Device** in the top right corner, select **Zigbee**, and then click **Start Zigbee Pairing**.
1. Click the pair button (🔗), located on the top of the device, 4 times in 5 seconds.
1. Return to the pairing page, provide a name for your device, and assign it to a room.
1. You're all set! Enjoy using your Vindstyrka Air Quality Sensor.

---
[<img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 40px !important;width: 162px !important">](https://www.buymeacoffee.com/dandanache)
