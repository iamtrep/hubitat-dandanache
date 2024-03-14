{{!--------------------------------------------------------------------------}}
{{# @definition }}
capability "PowerMeter"
capability "EnergyMeter"
{{/ @definition }}
{{!--------------------------------------------------------------------------}}
{{# @configure }}

// Configuration for capability.PowerMeter
cmds += "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0B04 {${device.zigbeeId}} {}" // Electrical Measurement cluster
cmds += "he cr 0x${device.deviceNetworkId} 0x01 0x0B04 0x050B 0x21 0x0000 0x4650 {02} {}" // Report ActivePower (uint16) at least every 5 hours (Δ = 0.2W)
cmds += zigbee.readAttribute(0x0B04, 0x0604, [destEndpoint:0x01]) // PowerMultiplier
cmds += zigbee.readAttribute(0x0B04, 0x0605, [destEndpoint:0x01]) // PowerDivisor

cmds += "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0702 {${device.zigbeeId}} {}" // (Metering (Smart Energy) cluster
cmds += "he cr 0x${device.deviceNetworkId} 0x01 0x0702 0x0000 0x25 0x0000 0x4650 {00} {}" // Report CurrentSummationDelivered (uint48) at least every 5 hours (Δ = 0)
cmds += zigbee.readAttribute(0x0702, 0x0301, [destEndpoint:0x01]) // Multiplier
cmds += zigbee.readAttribute(0x0702, 0x0302, [destEndpoint:0x01]) // Divisor
{{/ @configure }}
{{!--------------------------------------------------------------------------}}
{{# @events }}

// Events for capability.PowerMeter

// Report/Read Attributes Reponse: ActivePower
case { contains it, [clusterInt:0x0B04, commandInt:0x0A, attrInt:0x050B] }:
case { contains it, [clusterInt:0x0B04, commandInt:0x01, attrInt:0x050B] }:
    def power = Integer.parseInt(msg.value, 16) * (state.powerMultiplier ?: 1) / (state.powerDivisor ?: 1)
    Utils.sendEvent name:"power", value:power, unit:"W", descriptionText:"Power is ${power} W", type:type
    return Utils.processedZclMessage("${msg.commandInt == 0x0A ? "Report" : "Read"} Attributes Response", "ActivePower=${msg.value}")

// Report/Read Attributes Reponse: EnergySummation
case { contains it, [clusterInt:0x0702, commandInt:0x0A, attrInt:0x0000] }:
case { contains it, [clusterInt:0x0702, commandInt:0x01, attrInt:0x0000] }:
    def energy = Integer.parseInt(msg.value, 16) * (state.energyMultiplier ?: 1) / (state.energyDivisor ?: 1)
    Utils.sendEvent name:"energy", value:energy, unit:"kWh", descriptionText:"Energy is ${energy} kWh", type:type
    return Utils.processedZclMessage("${msg.commandInt == 0x0A ? "Report" : "Read"} Attributes Response", "EnergySummation=${msg.value}")

// Read Attributes Reponse: PowerMultiplier
case { contains it, [clusterInt:0x0B04, commandInt:0x01, attrInt:0x0604] }:
    state.powerMultiplier = Integer.parseInt(msg.value, 16)
    return Utils.processedZclMessage("Read Attributes Response", "PowerMultiplier=${msg.value}")

// Read Attributes Reponse: PowerDivisor
case { contains it, [clusterInt:0x0B04, commandInt:0x01, attrInt:0x0605] }:
    state.powerDivisor = Integer.parseInt(msg.value, 16)
    return Utils.processedZclMessage("Read Attributes Response", "PowerDivisor=${msg.value}")

// Read Attributes Reponse: EnergyMultiplier
case { contains it, [clusterInt:0x0702, commandInt:0x01, attrInt:0x0301] }:
    state.energyMultiplier = Integer.parseInt(msg.value, 16)
    return Utils.processedZclMessage("Read Attributes Response", "EnergyMultiplier=${msg.value}")

// Read Attributes Reponse: EnergyDivisor
case { contains it, [clusterInt:0x0702, commandInt:0x01, attrInt:0x0302] }:
    state.energyDivisor = Integer.parseInt(msg.value, 16)
    return Utils.processedZclMessage("Read Attributes Response", "EnergyDivisor=${msg.value}")

// Other events that we expect but are not usefull for capability.PowerMeter behavior
case { contains it, [clusterInt:0x0B04, commandInt:0x07] }:
    return Utils.processedZclMessage("Configure Reporting Response", "attribute=ActivePower, data=${msg.data}")
case { contains it, [clusterInt:0x0702, commandInt:0x07] }:
    return Utils.processedZclMessage("Configure Reporting Response", "attribute=CurrentSummation, data=${msg.data}")
{{/ @events }}
{{!--------------------------------------------------------------------------}}
