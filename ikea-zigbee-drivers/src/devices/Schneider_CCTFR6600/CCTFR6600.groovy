{{!--------------------------------------------------------------------------}}
{{# @definition }}
capability 'Actuator'
capability 'SignalStrength'
{{/ @definition }}
{{!--------------------------------------------------------------------------}}
{{# @fields }}

// Fields for devices.Schneider_CCTFR6600
@Field static final List<String> ZONES = [
    'zone_1', 'zone_2', 'zone_3', 'zone_4', 'zone_5', 'zone_6'
]
{{/ @fields }}
{{!--------------------------------------------------------------------------}}
{{# @attributes }}

// Attributes for devices.Schneider_CCTFR6600
attribute 'zone_1', 'enum', ['on', 'off']
attribute 'zone_2', 'enum', ['on', 'off']
attribute 'zone_3', 'enum', ['on', 'off']
attribute 'zone_4', 'enum', ['on', 'off']
attribute 'zone_5', 'enum', ['on', 'off']
attribute 'zone_6', 'enum', ['on', 'off']
attribute 'pump', 'enum', ['on', 'off']
attribute 'boiler', 'enum', ['on', 'off']
attribute 'debug', 'string'
{{/ @attributes }}
{{!--------------------------------------------------------------------------}}
{{# @commands }}

// Commands for devices.Schneider_CCTFR6600
command 'on', [[name:'Zone*', type:'ENUM', description:'Zone to turn On', constraints:ZONES]]
command 'off', [[name:'Zone*', type:'ENUM', description:'Zone to turn Off', constraints:ZONES]]
command 'exec', [[name:'Zigbee command', description:'Enter raw command to execute (e.g. for toggle on/off: he raw .addr 0x01 0x01 0x0006 {114302})', type:'STRING']]
{{/ @commands }}
{{!--------------------------------------------------------------------------}}
{{# @configure }}

// Configuration for Schneider_CCTFR6600.Switch
ZONES.each { off it }

cmds += "zdo bind 0x${device.deviceNetworkId} 0x07 0x01 0x0006 {${device.zigbeeId}} {}" // Pump endpoint
//cmds += "he cr 0x${device.deviceNetworkId} 0x07 0x0006 0x0000 0x10 0x0000 0x0258 {01} {}" // Report pump status at least every 10 minutes

cmds += "zdo bind 0x${device.deviceNetworkId} 0x08 0x01 0x0006 {${device.zigbeeId}} {}" // Boiler endpoint
//cmds += "he cr 0x${device.deviceNetworkId} 0x08 0x0006 0x0000 0x10 0x0000 0x0258 {01} {}" // Report boiler status at least every 10 minutes

ZONES.each {
    cmds += "he cr 0x${device.deviceNetworkId} 0x0${it.substring(5)} 0x0006 0x0000 0x10 0x0000 0x0258 {01} {}" // Report zones[1-6] status at least every 10 minutes
}
{{/ @configure }}
{{!--------------------------------------------------------------------------}}
{{# @refresh }}

// Refresh for devices.Schneider_CCTFR6600
cmds += zigbee.readAttribute(0xFE03, 0x0020, [mfgCode: '0x105E']) // Wiser Debug Info

ZONES.each { cmds += zigbee.readAttribute(0x0006, 0x0000, [destEndpoint:Integer.parseInt(it.substring(5))]) } // Zones[1-6] OnOff
cmds += zigbee.readAttribute(0x0006, 0x0000, [destEndpoint:0x07]) // Pump OnOff
cmds += zigbee.readAttribute(0x0006, 0x0000, [destEndpoint:0x08]) // Boiler OnOff
{{/ @refresh }}
{{!--------------------------------------------------------------------------}}
{{# @implementation }}

// Implementation for devices.Schneider_CCTFR6600
void on(String zone) {
    if (!ZONES.contains(zone)) {
        log_error "Invalid zone: ${zone}. Available zones: ${ZONES}"
        return
    }
    utils_sendEvent name:zone, value:'on', descriptionText:"Zone ${zone} was turned on", type:'digital'
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x0${zone.substring(5)} 0x0006 {014301}"])
}

void off(String zone) {
    if (!ZONES.contains(zone)) {
        log_error "Invalid zone: ${zone}. Available zones: ${ZONES}"
        return
    }
    utils_sendEvent name:zone, value:'off', descriptionText:"Zone ${zone} was turned off", type:'digital'
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x0${zone.substring(5)} 0x0006 {014300}"])
}

void exec(String command) {
    log_info "Exec: ${command}"
    String cmd = command.replace '.addr', "0x${device.deviceNetworkId}"
    utils_sendZigbeeCommands([cmd])
}

private String attrValue(Integer attr, Integer type, Integer value, Integer bytes) {
    return "${utils_payload attr, 4}00${utils_payload type, 2}${utils_payload value, bytes}"
}
{{/ @implementation }}
{{!--------------------------------------------------------------------------}}
{{# @events }}

// Events for devices.Schneider_CCTFR6600
// ===================================================================================================================

// Read Attributes: ZCL Version
case { contains it, [clusterInt:0x0000, commandInt:0x00, data:['00', '00']] }:
    Integer frameControl = 0x08
    Integer txSeq = 0x00
    Integer command = 0x01 // Read Attributes Response
    String payload = attrValue(0x0000, 0x20, 0x03, 2) // ZCL Version = 0x03

    // Send response only once every 5 minutes
    //if (Calendar.getInstance().get(Calendar.MINUTE) % 5 == 0)
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0000 {${utils_payload frameControl, 2}${utils_payload txSeq, 2}${utils_payload command, 2} ${payload}}"])
    utils_processedZclMessage '😊 Read Attributes (health check)', "endpoint=${msg.sourceEndpoint}, cluster=Basic, attr=0000 (ZCL Version)"
    return

// ▶ Processed ZCL message: type=Read Attributes, endpoint=03, manufacturer=0000, cluster=0006, attrs=[0000]
case { contains it, [clusterInt:0x0006, commandInt:0x00, data:['00', '00']] }:
    String zone = "zone_${Integer.parseInt msg.sourceEndpoint, 16}"
    boolean status = device.currentValue(zone, true) == 'on'

    Integer frameControl = Integer.parseInt('00001000', 2)
    Integer txSeq = 0x00
    Integer command = 0x01 // Read Attributes Response
    String payload = attrValue(0x0000, 0x10, status ? 0x01 : 0x00, 2)
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x${msg.sourceEndpoint} 0x0006 {${utils_payload frameControl, 2}${utils_payload txSeq, 2}${utils_payload command, 2} ${payload}}"])
    utils_processedZclMessage '😊 Read Attributes', "endpoint=${msg.sourceEndpoint}, cluster=OnOff, attr=0000 (OnOff)"
    return

// ▶ Processed ZCL message: type=Read Attributes, endpoint=03, manufacturer=105E, cluster=0006, attrs=[E002]
case { contains it, [clusterInt:0x0006, commandInt:0x00, data:['02', 'E0']] }:
    Integer frameControl = Integer.parseInt('00001100', 2)
    Integer txSeq = 0x00
    Integer command = 0x01 // Read Attributes Response
    String payload = attrValue(0xE002, 0x10, 0x00, 2)
    Integer mfgCode = 0x105E
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x${msg.sourceEndpoint} 0x0006 {${utils_payload frameControl, 2}${utils_payload mfgCode, 4}${utils_payload txSeq, 2}${utils_payload command, 2} ${payload}}"])
    utils_processedZclMessage '😊 Read Attributes', "endpoint=${msg.sourceEndpoint}, cluster=OnOff, attr=E002 (Unknown)"
    return

// ▶ Processed ZCL message: type=Read Attributes, endpoint=03, manufacturer=105E, cluster=FF16, attrs=[0000, 0001, 0002, 0010, 0011, 0012, 0030]
case { contains it, [clusterInt:0xFF16, commandInt:0x00, data:['00', '00', '01', '00', '02', '00', '10', '00', '11', '00', '12', '00', '30', '00']] }:
    Integer frameControl = Integer.parseInt('00001100', 2)
    Integer txSeq = 0x00
    Integer command = 0x01 // Read Attributes Response
    String payload = ''
    payload += attrValue(0x0000, 0x20, 0xC8, 2) + ' '   // 0x0000 : 0x64 = 100  (uint8)
    payload += '010086 '                                // 0x0001 : UNSUPPORTED_ATTRIBUTE
    payload += '020086 '                                // 0x0002 : UNSUPPORTED_ATTRIBUTE
    payload += attrValue(0x0010, 0x21, 0x04B0, 4) + ' ' // 0x0010 : 0258 = 600  (uint16)
    payload += attrValue(0x0011, 0x21, 0x0258, 4) + ' ' // 0x0011 : 012C = 300  (uint16)
    payload += attrValue(0x0012, 0x21, 0x1C20, 4) + ' ' // 0x0012 : 0E10 = 3600 (uint16)
    payload += attrValue(0x0030, 0x20, 0x42, 2)         // 0x0030 : 0x21 = 33   (uint8)

    Integer mfgCode = 0x105E
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x${msg.sourceEndpoint} 0xFF16 {${utils_payload frameControl, 2}${utils_payload mfgCode, 4}${utils_payload txSeq, 2}${utils_payload command, 2} ${payload}}"])
    utils_processedZclMessage '😊 Read Attributes', "endpoint=${msg.sourceEndpoint}, cluster=FF16, attrs=[0000, 0001, 0002, 0010, 0011, 0012, 0030]"
    return

// ▶ Processed ZCL message: type=Read Attributes, endpoint=01, manufacturer=0000, cluster=0201, attrs=[001C, 0015, 0016, 0017, 0018]
case { contains it, [clusterInt:0x0201, commandInt:0x00, data:['1C', '00', '15', '00', '16', '00', '17', '00', '18', '00']] }:
    Integer frameControl = Integer.parseInt('00001100', 2)
    Integer txSeq = 0x00
    Integer command = 0x01 // Read Attributes Response
    String payload = ''
    payload += attrValue(0x001C, 0x30, 0x04, 2) + ' '   // 0x0000 : 0x03 = Cool  (enum8)     | 0x00:Off, 0x01:Auto, 0x03:Cool, 0x04:Heat, 0x05:Emergency heating
                                                        //                                   | 0x06:Precooling, 0x07:Fan only, 0x08:Dry, 0x09:Sleep
    payload += attrValue(0x0015, 0x29, 0x954D, 4) + ' ' // 0x0015 : 954D = -54.53°C (int16)  | Min Heat Setpoint Limit
    payload += attrValue(0x0016, 0x29, 0x7FFF, 4) + ' ' // 0x0016 : 7FFF = 327.67°C (int16)  | Max Heat Setpoint Limit
    payload += attrValue(0x0017, 0x29, 0x954D, 4) + ' ' // 0x0017 : 954D = -54.53°C (int16)  | Min Cool Setpoint Limit
    payload += attrValue(0x0038, 0x29, 0x7FFF, 4)       // 0x0018 : 7FFF = 327.67°C (int16)  | Max Cool Setpoint Limit

    Integer mfgCode = 0x105E
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x01 0x0201 {${utils_payload frameControl, 2}${utils_payload mfgCode, 4}${utils_payload txSeq, 2}${utils_payload command, 2} ${payload}}"])
    utils_processedZclMessage '😊 Read Attributes', "endpoint=${msg.sourceEndpoint}, cluster=Thermostat, attrs=[001C, 0015, 0016, 0017, 0018]"
    return

// Report/Read Attributes: Pump
case { contains it, [endpointInt:0x07, clusterInt:0x0006, commandInt:0x0A, attrInt:0x0000] }:
case { contains it, [endpointInt:0x07, clusterInt:0x0006, commandInt:0x01, attrInt:0x0000] }:
    String pump = msg.value == '00' ? 'off' : 'on'
    utils_sendEvent name:'pump', value:pump, descriptionText:"Pump was turned ${pump}", type:type
    utils_processedZclMessage "${msg.commandInt == 0x0A ? 'Report' : 'Read'} Attributes Response", "Pump=${pump}"
    return

// Report/Read Attributes: Boiler
case { contains it, [endpointInt:0x08, clusterInt:0x0006, commandInt:0x0A, attrInt:0x0000] }:
case { contains it, [endpointInt:0x08, clusterInt:0x0006, commandInt:0x01, attrInt:0x0000] }:
    String boiler = msg.value == '00' ? 'off' : 'on'
    utils_sendEvent name:'boiler', value:boiler, descriptionText:"Boiler was turned ${boiler}", type:type
    utils_processedZclMessage "${msg.commandInt == 0x0A ? 'Report' : 'Read'} Attributes Response", "Boiler=${boiler}"
    return

// Read Attributes: Zone 1-6
case { contains it, [clusterInt:0x0006, commandInt:0x01, attrInt:0x0000] }:
    String zone = "zone_${msg.endpointInt}"
    String status = msg.value == '01' ? 'on' : 'off'
    utils_sendEvent name:zone, value:status, descriptionText:"Zone ${zone} is ${status}", type:'digital'
    utils_processedZclMessage 'Read Attributes Response', "${zone}=${status}"
    return

// Read Attributes Reponse: Debug Info
case { contains it, [clusterInt:0xFE03, commandInt:0x01, attrInt:0x0020] }:
case { contains it, [clusterInt:0xFE03, commandInt:0x0A, attrInt:0x0020] }:
    utils_sendEvent name:'debug', value:msg.value, descriptionText:"Debug info is ${msg.value}", type:type
    utils_processedZclMessage "${msg.commandInt == 0x0A ? 'Report' : 'Read'} Attributes Response", "WiserDebugInfo=${msg.value}"
    return

// Report Attributes: LastMessageLQI
case { contains it, [clusterInt:0x0B05, commandInt:0x0A, attrInt:0x011C] }:
    Integer lqi = Integer.parseInt msg.value, 16
    utils_sendEvent name:'lqi', value:lqi, descriptionText:"Signal LQI is ${lqi}", type:'physical'
    msg.additionalAttrs.each {
        if (it.attrId == '011D') {
            Integer rssi = Integer.parseInt it.value, 16
            utils_sendEvent name:'rssi', value:rssi, descriptionText:"Signal RSSI is ${rssi}", type:'physical'
            utils_processedZclMessage 'Report Attributes Response', "Diagnostics/LastMessageRSSI=${it.value}"
        }
    }
    utils_processedZclMessage 'Report Attributes Response', "Diagnostics/LastMessageLQI=${msg.value}"
    return

// Report Unhandled Attributes
case { contains it, [commandInt:0x0A] }:
    List<String> attrs = ["${msg.attrId}=${msg.value} (${msg.encoding})"]
    msg.additionalAttrs?.each { attrs += "${it.attrId}=${it.value} (${it.encoding})" }
    utils_processedZclMessage '👿 Report Attributes', "endpoint=${msg.sourceEndpoint ?: msg.endpoint}, manufacturer=${msg.manufacturerId ?: '0000'}, cluster=${msg.clusterId ?: msg.cluster}, attrs=${attrs}"
    return

// Read Unhandled Attributes
case { contains it, [commandInt:0x00] }:
    List<String> attrs = msg.data.collate(2).collect { "${it.reverse().join()}" }
    utils_processedZclMessage '👿 Read Attributes', "endpoint=${msg.sourceEndpoint ?: msg.endpoint}, manufacturer=${msg.manufacturerId ?: '0000'}, cluster=${msg.clusterId ?: msg.cluster}, attrs=${attrs}"
    return

// Other events that we expect but are not usefull
case { contains it, [endpointInt:0x07, clusterInt:0x0006, commandInt:0x07] }:
    utils_processedZclMessage 'Configure Reporting Response', "attribute=Pump, data=${msg.data}"
    return
case { contains it, [endpointInt:0x08, clusterInt:0x0006, commandInt:0x07] }:
    utils_processedZclMessage 'Configure Reporting Response', "attribute=Boiler, data=${msg.data}"
    return
{{/ @events }}
{{!--------------------------------------------------------------------------}}
