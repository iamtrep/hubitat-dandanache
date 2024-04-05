{{!--------------------------------------------------------------------------}}
{{# @configure }}

// Configuration for devices.Ikea_E2002
cmds += "zdo bind 0x${device.deviceNetworkId} 0x${device.endpointId} 0x01 0x0005 {${device.zigbeeId}} {}" // Scenes cluster
cmds += "zdo bind 0x${device.deviceNetworkId} 0x${device.endpointId} 0x01 0x0006 {${device.zigbeeId}} {}" // On/Off cluster
cmds += "zdo bind 0x${device.deviceNetworkId} 0x${device.endpointId} 0x01 0x0008 {${device.zigbeeId}} {}" // Level Control cluster
{{/ @configure }}
{{!--------------------------------------------------------------------------}}
{{# @events }}

// Events for devices.Ikea_E2002
// ===================================================================================================================

// Plus/Minus button was pushed
case { contains it, [clusterInt:0x0006, commandInt:0x00] }:
case { contains it, [clusterInt:0x0006, commandInt:0x01] }:
    List<String> button = msg.commandInt == 0x00 ? BUTTONS.MINUS : BUTTONS.PLUS
    utils_sendEvent name:'pushed', value:button[0], type:'physical', isStateChange:true, descriptionText:"Button ${button[0]} (${button[1]}) was pushed"
    return

// Plus/Minus button was held
case { contains it, [clusterInt:0x0008, commandInt:0x01] }:
case { contains it, [clusterInt:0x0008, commandInt:0x05] }:
    List<String> button = msg.commandInt == 0x01 ? BUTTONS.MINUS : BUTTONS.PLUS
    utils_sendEvent name:'held', value:button[0], type:'physical', isStateChange:true, descriptionText:"Button ${button[0]} (${button[1]}) was held"
    return

// Plus/Minus button was released
case { contains it, [clusterInt:0x0008, commandInt:0x07] }:
case { contains it, [clusterInt:0x0008, commandInt:0x03] }:
    List<String> button = device.currentValue('held', true) == 2 || msg.commandInt == 0x03 ? BUTTONS.MINUS : BUTTONS.PLUS
    utils_sendEvent name:'released', value:button[0], type:'physical', isStateChange:true, descriptionText:"Button ${button[0]} (${button[1]}) was released"
    return

// Next/Prev button was pushed
case { contains it, [clusterInt:0x0005, commandInt:0x07] }:
    List<String> button = msg.data[0] == '00' ? BUTTONS.NEXT : BUTTONS.PREV
    utils_sendEvent name:'pushed', value:button[0], type:'physical', isStateChange:true, descriptionText:"Button ${button[0]} (${button[1]}) was pushed"
    return

/*
Holding the PREV and NEXT buttons works in a weird way:

When PREV button is held, the following Zigbee messages are received:
#1. [12:55:35.429] description=[catchall: 0104 0005 01 01 0040 00 926B 01 01 117C 09 00 0000]
#2. [12:55:35.908] description=[catchall: 0104 0006 01 01 0040 00 926B 01 00 0000 01 00 ]             <-- button 1 (🔆) was pushed [physical]
#3. [12:55:36.422] description=[catchall: 0104 0005 01 01 0040 00 926B 01 00 0000 05 00 0000000000]
#4. [12:55:37.411] description=[catchall: 0104 0005 01 01 0040 00 926B 01 01 117C 08 00 010D00]

When PREV button is released, the following Zigbee message is received:
#5. [on release]   description=[catchall: 0104 0005 01 01 0040 00 926B 01 01 117C 09 00 XXXX]

When NEXT button is held, the following Zigbee messages are received:
#1. [12:56:59.463] description=[catchall: 0104 0005 01 01 0040 00 926B 01 01 117C 09 00 0000]
#2. [12:56:59.962] description=[catchall: 0104 0006 01 01 0040 00 926B 01 00 0000 01 00 ]             <-- button 1 (🔆) was pushed [physical]
#3. [12:57:00.480] description=[catchall: 0104 0005 01 01 0040 00 926B 01 00 0000 05 00 0000000000]
#4. [12:57:01.466] description=[catchall: 0104 0005 01 01 0040 00 926B 01 01 117C 08 00 000D00]

When NEXT button is released, the following Zigbee message is received:
#5. [on release]   description=[catchall: 0104 0005 01 01 0040 00 926B 01 01 117C 09 00 XXXX]

There is at least 2 seconds delay between the moment the device figured out that the button is held (not a click)
and the moment message #4 is received (the moment we can figure out what button was held (010D00 vs 000D00)).

IMHO, this weird behavior makes the use of the hold actions on the PREV and NEXT button unusable.
*/
{{/ @events }}
{{!--------------------------------------------------------------------------}}
