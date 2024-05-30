{{!--------------------------------------------------------------------------}}
{{# @fields }}

// Fields for devices.NodOn_SIN-4-1-20
@Field static final Map<Integer, String> PULSE_DURATIONS = [
       '0':'Disable Impulse Mode',
     '100':'100 miliseconds',
     '300':'300 miliseconds',
     '500':'500 miliseconds',
    '1000':'1 second',
    '2000':'2 seconds',
]
{{/ @fields }}
{{!--------------------------------------------------------------------------}}
{{# @attributes }}

// Attributes for devices.NodOn_SIN-4-1-20
attribute 'switchType', 'enum', ['toggle', 'momentary']
{{/ @attributes }}
{{!--------------------------------------------------------------------------}}
{{# @inputs }}
input(
    name: 'pulseDuration', type: 'enum',
    title: 'Impulse Mode',
    description: '<small>Disable Inpulse Mode or configure pulse duration.</small>',
    options: PULSE_DURATIONS,
    defaultValue: '0',
    required: true
)
{{/ @inputs }}
{{!--------------------------------------------------------------------------}}
{{# @updated }}
if (pulseDuration == null) {
    pulseDuration = '0'
    device.updateSetting 'pulseDuration', [value:pulseDuration, type:'enum']
}
log_info "🛠️ pulseDuration = ${pulseDuration}ms"
cmds += zigbee.writeAttribute(0x0006, 0x0001, 0x21, Integer.parseInt(pulseDuration), [mfgCode:'0x128B', destEndpoint:0x01])
{{/ @updated }}
{{!--------------------------------------------------------------------------}}
{{# @refresh }}

// Refresh for devices.NodOn_SIN-4-1-20
cmds += zigbee.readAttribute(0x0006, 0x0001, [mfgCode: '0x128B']) // TransitionTime
cmds += zigbee.readAttribute(0x0007, 0x0000) // SwitchType
{{/ @refresh }}
{{!--------------------------------------------------------------------------}}
{{# @events }}

// Events for devices.NodOn_SIN-4-1-20
// ===================================================================================================================

// Read Attributes: TransitionTime
case { contains it, [clusterInt:0x0006, commandInt:0x01, attrInt:0x0001] }:
case { contains it, [clusterInt:0x0006, commandInt:0x0A, attrInt:0x0001] }:
    String pulseDuration = Integer.parseInt(msg.value, 16).toString()
    if (!PULSE_DURATIONS.containsKey(pulseDuration)) pulseDuration = '0'
    device.updateSetting 'pulseDuration', [value:pulseDuration, type:'enum']
    utils_processedZclMessage 'Read Attributes Response', "TransitionTime=${msg.value} (${pulseDuration}ms)"
    return

// Read Attributes: SwitchType
case { contains it, [clusterInt:0x0007, commandInt:0x01, attrInt:0x0000] }:
case { contains it, [clusterInt:0x0007, commandInt:0x0A, attrInt:0x0000] }:
    String switchType = msg.value == '00' ? 'toggle' : 'momentary'
    utils_sendEvent name:'switchType', value:switchType, descriptionText:"Switch type is ${switchType}", type:type
    utils_processedZclMessage 'Read Attributes Response', "SwitchType=${msg.value} (${switchType})"
    return

// Other events that we expect but are not usefull
case { contains it, [clusterInt:0x0006, commandInt:0x04] }: // Write Attributes: weird event when switch state changes
case { contains it, [clusterInt:0x0006, commandInt:0x02] }: // Write Attributes Response
    return
{{/ @events }}
{{!--------------------------------------------------------------------------}}
