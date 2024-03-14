{{!--------------------------------------------------------------------------}}
{{# @definition }}
capability "Actuator"
{{/ @definition }}
{{!--------------------------------------------------------------------------}}
{{# @fields }}

// Fields for capability.MultiRelay
import com.hubitat.app.ChildDeviceWrapper
{{/ @fields }}
{{!--------------------------------------------------------------------------}}
{{# @implementation }}

// Implementation for capability.MultiRelay
private ChildDeviceWrapper fetchChildDevice(Integer moduleNumber){
    def childDevice = getChildDevice("${device.deviceNetworkId}-${moduleNumber}")
    if (!childDevice) {
        childDevice = addChildDevice("hubitat", "Generic Component Switch", "${device.deviceNetworkId}-${moduleNumber}", [name:"${device.displayName} - Relay L${moduleNumber}", label:"Relay L${moduleNumber}", isComponent:true])
        childDevice.parse([[name:"switch", value:"off", descriptionText:"Set initial switch value"]])
    }
    return childDevice
}

void componentOff(childDevice) {
    Log.debug "▲ Received Off request from ${childDevice.displayName}"
    Integer endpointInt = Integer.parseInt(childDevice.deviceNetworkId.split('-')[1])
    Utils.sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x0${endpointInt} 0x0006 {014300}"])
}

void componentOn(childDevice) {
    Log.debug "▲ Received On request from ${childDevice.displayName}"
    Integer endpointInt = Integer.parseInt(childDevice.deviceNetworkId.split('-')[1])
    Utils.sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x0${endpointInt} 0x0006 {014301}"])
}

void componentRefresh(childDevice) {
    Log.debug "▲ Received Refresh request from ${childDevice.displayName}"
    tryToRefresh()
}
{{/ @implementation }}
{{!--------------------------------------------------------------------------}}
{{# @configure }}

// Configuration for capability.MultiRelay
cmds += "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}" // On/Off cluster
cmds += "zdo bind 0x${device.deviceNetworkId} 0x02 0x01 0x0006 {${device.zigbeeId}} {}" // On/Off cluster

cmds += "he cr 0x${device.deviceNetworkId} 0x01 0x0006 0x0000 0x10 0x0000 0x0258 {01} {}" // Report OnOff (bool) at least every 10 minutes
cmds += "he cr 0x${device.deviceNetworkId} 0x02 0x0006 0x0000 0x10 0x0000 0x0258 {01} {}" // Report OnOff (bool) at least every 10 minutes

cmds += "he raw 0x${device.deviceNetworkId} 0x01 0x01 0x0006 {104300 0000}" // Read OnOff attribute
cmds += "he raw 0x${device.deviceNetworkId} 0x02 0x01 0x0006 {104400 0000}" // Read OnOff attribute
{{/ @configure }}
{{!--------------------------------------------------------------------------}}
{{# @events }}

// Events for capability.MultiRelay

// Report/Read Attributes: OnOff
case { contains it, [clusterInt:0x0006, commandInt:0x0A, attrInt:0x0000] }:
case { contains it, [clusterInt:0x0006, commandInt:0x01, attrInt:0x0000] }:
    Integer moduleNumber = msg.endpointInt
    String newState = msg.value == "00" ? "off" : "on"

    // Send event to module child device (only if state needs to change)
    def childDevice = fetchChildDevice(moduleNumber)
    if (newState != childDevice.currentValue("switch", true)) {
        childDevice.parse([[name:"switch", value:newState, descriptionText:"${childDevice.displayName} was turned ${newState}", type:type]])
    }

    return Utils.processedZclMessage("${msg.commandInt == 0x0A ? "Report" : "Read"} Attributes Response", "Module=${moduleNumber}, Switch=${newState}")

// Other events that we expect but are not usefull for capability.MultiRelay behavior
case { contains it, [clusterInt:0x0006, commandInt:0x07] }:
    return Utils.processedZclMessage("Configure Reporting Response", "attribute=switch, data=${msg.data}")
{{/ @events }}
{{!--------------------------------------------------------------------------}}
