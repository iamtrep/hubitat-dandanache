{{!--------------------------------------------------------------------------}}
{{# @inputs }}

// Inputs for devices.Ikea_E2204
input(
    name: 'childLock', type: 'bool',
    title: 'Child lock',
    description: '<small>Lock physical button, safeguarding against accidental operation.</small>',
    defaultValue: false
)
input(
    name: 'darkMode', type: 'bool',
    title: 'Dark mode',
    description: '<small>Turn off LED indicators on the device, ensuring total darkness.</small>',
    defaultValue: false
)
{{/ @inputs }}
{{!--------------------------------------------------------------------------}}
{{# @updated }}

// Preferences for devices.Ikea_E2204
if (childLock == null) {
    childLock = false
    device.updateSetting 'childLock', [value:childLock, type:'bool']
}
log_info "🛠️ childLock = ${childLock}"
cmds += zigbee.writeAttribute(0xFC85, 0x0000, 0x10, childLock ? 0x01 : 0x00, [mfgCode:'0x117C'])

if (darkMode == null) {
    darkMode = false
    device.updateSetting 'darkMode', [value:darkMode, type:'bool']
}
log_info "🛠️ darkMode = ${darkMode}"
cmds += zigbee.writeAttribute(0xFC85, 0x0001, 0x10, darkMode ? 0x01 : 0x00, [mfgCode:'0x117C'])
{{/ @updated }}
{{!--------------------------------------------------------------------------}}
{{# @refresh }}

// Refresh for devices.Ikea_E2204
cmds += zigbee.readAttribute(0xFC85, 0x0000, [mfgCode:'0x117C'] ) // ChildLock
cmds += zigbee.readAttribute(0xFC85, 0x0001, [mfgCode:'0x117C'] ) // DarkMode
{{/ @refresh }}
{{!--------------------------------------------------------------------------}}
{{# @events }}

// Events for devices.Ikea_E2204
// ===================================================================================================================

// Read Attributes: ChildLock
case { contains it, [clusterInt:0xFC85, commandInt:0x01, attrInt:0x0000] }:
    childLock = msg.value == '01'
    device.updateSetting 'childLock', [value:childLock, type:'bool']
    utils_processedZclMessage 'Read Attributes Response', "ChildLock=${msg.value}"
    return

// Read Attributes: DarkMode
case { contains it, [clusterInt:0xFC85, commandInt:0x01, attrInt:0x0001] }:
    darkMode = msg.value == '01'
    device.updateSetting 'darkMode', [darkMode:childLock, type:'bool']
    utils_processedZclMessage 'Read Attributes Response', "DarkMode=${msg.value}"
    return

// Write Attributes Response
case { contains it, [endpointInt:0x01, clusterInt:0xFC85, commandInt:0x04, isClusterSpecific:false, isManufacturerSpecific:true, manufacturerId:'117C'] }:
    return
{{/ @events }}
{{!--------------------------------------------------------------------------}}
