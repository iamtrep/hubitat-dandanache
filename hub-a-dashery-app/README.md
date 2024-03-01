# Hub-a-Dashery

<img src="icon.png" style="height: 32px !important; weight: 32px !important; float: right; margin-bottom: 10px">
View dashboards for your Hubitat hub metrics.

## Installation

To install the Hub-a-Dashery app using the Hubitat Package Manager (and receive automatic updates), follow these steps:

1. Go to the **Apps** menu in the Hubitat interface.
2. Select **Hubitat Package Manager** from the list of apps.
3. Click **Install** and then **Search by Keywords**.
4. Type **Hub-a-Dashery** in the search box and click **Next**.
5. Choose **Hub-a-Dashery by Dan Danache** and click **Next**.
6. Read the license agreement and click **Next**.
7. Wait for the installation to complete and click **Next**.
8. Go back to the **Apps** menu in the Hubitat interface.
9. Click the **Add user app** button in the top right corner.
10. Select **Hub-a-Dashery** from the list of apps.

## Usage

To use the Hub-a-Dashery app, follow these steps:

1. Go to the **Apps** menu in the Hubitat interface.
2. Select **Hub-a-Dashery** from the list of apps.

## Features

### Dark Theme Support
The dark theme reduces the luminance emitted by device screens, while still meeting minimum color contrast ratios. It helps improve visual ergonomics by reducing eye strain and facilitating screen use in dark environments – all while conserving battery power.

You can enable/disable the dark theme from the Hubitat app:

![Dark Theme](img/dark-theme.png "Enable/disable dark theme")

### Dashboard Grid Layout
The app provides a flexible and user-friendly dashboard grid layout that empowers you to customize your dashboard experience. With this feature, you can effortlessly rearrange charts, widgets, and other visual elements to suit your preferences.

When you’ve achieved the ideal arrangement, press **Ctrl + S** to save the current grid layout.

![Dashboard Grid Layout](img/grid-layout.png "Dashboard Grid Layout")

### Zoom and Pan
You can use the zoom and pan functionality for charts that display multiple data points over time.

#### Zooming In and Out
- To **zoom in**, use the mouse wheel (scroll up) or perform a pinch gesture on mobile devices. This action magnifies the chart, allowing you to focus on specific data points or intervals.
- Conversely, to **zoom out**, scroll the mouse wheel down or reverse the pinch gesture. Zooming out provides a broader view of the chart, encompassing a larger time range or dataset.

#### Panning (Horizontal Movement):
Panning allows you to explore different time periods within the same chart. Here’s how it works:

- **Left Panning**: Drag the chart to the left (by clicking and holding while moving the cursor left). This exposes later data points or shifts the view to the future.

- **Right Panning**: Drag the chart to the right (similarly, click and hold while moving the cursor right). This reveals earlier data points or shifts the view to the past.

These interactive features enhance your ability to analyze data dynamically. Feel free to experiment with zooming and panning to uncover insights hidden within your charts!

![Zoom and Pan](img/zoom-and-pan.png "Zoom and Pan")

## Dashboard Widgets

The app provides various widgets to monitor the Hub's performance and status. Currently, the following dashboards widgets are available:

### Memory and CPU information

These 4 widgets show the OS free memory (RAM) and the processor [load average](https://phoenixnap.com/kb/linux-average-load) of the Hub over time and in the last 15 minutes.

Refresh time: 1 min

You can access the data source at:
- `http://hubitat.local/hub/advanced/freeOSMemoryHistory`
- `http://hubitat.local/hub/advanced/hub/advanced/freeOSMemoryLast`

**Note**: The history data is reset after each Hub reboot and this data is not available for the first 15 minutes.

![Memory and CPU information](img/charts/mem-cpu.png "Memory and CPU widgets")

### Hub information

This simple table widget shows information about your Hubitat hub.

Refresh time: 5 min

![Hub information](img/charts/hub-info.png "Hub info widget")


---
[<img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 40px !important;width: 162px !important">](https://www.buymeacoffee.com/dandanache)
