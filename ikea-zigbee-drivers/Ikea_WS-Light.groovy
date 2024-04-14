/**
 * IKEA White Spectrum Light
 *
 * @see https://dan-danache.github.io/hubitat/ikea-zigbee-drivers/
 */
import groovy.transform.CompileStatic
import groovy.transform.Field

@Field static final String DRIVER_NAME = 'IKEA White Spectrum Light'
@Field static final String DRIVER_VERSION = '5.0.0'

// Fields for capability.HealthCheck
import groovy.time.TimeCategory

@Field static final Map<String, String> HEALTH_CHECK = [
    'schedule': '0 0 0/1 ? * * *', // Health will be checked using this cron schedule
    'thereshold': '3600' // When checking, mark the device as offline if no Zigbee message was received in the last 3600 seconds
]

// Fields for capability.ZigbeeGroups
@Field static final Map<String, String> GROUPS = [
    '9900':'Alfa', '9901':'Bravo', '9902':'Charlie', '9903':'Delta', '9904':'Echo', '9905':'Foxtrot', '9906':'Golf', '9907':'Hotel', '9908':'India', '9909':'Juliett', '990A':'Kilo', '990B':'Lima', '990C':'Mike', '990D':'November', '990E':'Oscar', '990F':'Papa', '9910':'Quebec', '9911':'Romeo', '9912':'Sierra', '9913':'Tango', '9914':'Uniform', '9915':'Victor', '9916':'Whiskey', '9917':'Xray', '9918':'Yankee', '9919':'Zulu'
]

metadata {
    definition(name:DRIVER_NAME, namespace:'dandanache', author:'Dan Danache', importUrl:'https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/Ikea_WS-Light.groovy') {
        capability 'Configuration'
        capability 'Refresh'
        capability 'Actuator'
        capability 'Switch'
        capability 'ColorTemperature'
        capability 'ColorMode'
        capability 'ChangeLevel'
        capability 'SwitchLevel'
        capability 'HealthCheck'
        capability 'PowerSource'

        fingerprint profileId:'0104', endpointId:'01', inClusters:'0000,0003,0004,0005,0006,0008,0300,1000,FC57', outClusters:'0019', model:'TRADFRI bulb E14 WS globe 470lm', manufacturer:'IKEA of Sweden' // LED2101G4: 1.1.003 (117C-2204-00011003)
        fingerprint profileId:'0104', endpointId:'01', inClusters:'0000,0003,0004,0005,0006,0008,0300,1000,FC57', outClusters:'0019', model:'TRADFRIbulbE14WScandleopal470lm', manufacturer:'IKEA of Sweden' // LED1949C5: 1.1.003 (117C-2204-00011003)
        
        // Attributes for capability.HealthCheck
        attribute 'healthStatus', 'enum', ['offline', 'online', 'unknown']
    }
    
    // Commands for capability.Switch
    command 'toggle'
    command 'onWithTimedOff', [[name:'On duration*', type:'NUMBER', description:'After how many seconds power will be turned Off [1..6500]']]
    
    // Commands for capability.ColorTemperature
    command 'startColorTemperatureChange', [[name:'Direction*', type:'ENUM', constraints: ['up', 'down']]]
    command 'stopColorTemperatureChange'
    command 'shiftColorTemperature', [[name:'Direction*', type:'ENUM', constraints: ['up', 'down']]]
    
    // Commands for capability.Brightness
    command 'shiftLevel', [[name:'Direction*', type:'ENUM', constraints: ['up', 'down']]]
    
    // Commands for capability.FirmwareUpdate
    command 'updateFirmware'

    preferences {
        input(
            name: 'helpInfo', type: 'hidden',
            title: '''
            <div style="min-height:55px; background:transparent url('https://dan-danache.github.io/hubitat/ikea-zigbee-drivers/img/Ikea_WS-Light.webp') no-repeat left center;background-size:auto 55px;padding-left:60px">
                IKEA White Spectrum Light <small>v5.0.0</small><br>
                <small><div>
                • <a href="https://dan-danache.github.io/hubitat/ikea-zigbee-drivers/#ws-light" target="_blank">device details</a><br>
                • <a href="https://community.hubitat.com/t/release-ikea-zigbee-drivers/123853" target="_blank">community page</a><br>
                </div></small>
            </div>
            '''
        )
        input(
            name: 'logLevel', type: 'enum',
            title: 'Log verbosity',
            description: '<small>Select what type of messages appear in the "Logs" section.</small>',
            options: ['1':'Debug - log everything', '2':'Info - log important events', '3':'Warning - log events that require attention', '4':'Error - log errors'],
            defaultValue: '1',
            required: true
        )
        
        // Inputs for capability.Switch
        input(
            name: 'powerOnBehavior',
            type: 'enum',
            title: 'Power On behaviour',
            description: '<small>Select what happens after a power outage.</small>',
            options: ['TURN_POWER_ON':'Turn power On', 'TURN_POWER_OFF':'Turn power Off', 'RESTORE_PREVIOUS_STATE':'Restore previous state'],
            defaultValue: 'RESTORE_PREVIOUS_STATE',
            required: true
        )
        
        // Inputs for capability.ColorTemperature
        input(
            name: 'colorTemperatureStep', type: 'enum',
            title: 'Color Temperature up/down shift',
            description: '<small>Color Temperature +/- adjust for the shiftColorTemperature() command.</small>',
            options: ['1':'1%', '2':'2%', '5':'5%', '10':'10%', '20':'20%', '25':'25%', '33':'33%', '50':'50%'],
            defaultValue: '25',
            required: true
        )
        input(
            name: 'colorTemperatureChangeRate', type: 'enum',
            title: 'Color Temperature change rate',
            description: '<small>Color Temperature +/- adjust for the startColorTemperatureChange() command.</small>',
            options: [
                 '10': '10% / sec - from hot to cold in 10 seconds',
                 '20': '20% / sec - from hot to cold in 5 seconds',
                 '33': '33% / sec - from hot to cold in 3 seconds',
                 '50': '50% / secs - from hot to cold in 2 seconds',
                '100': '100% / sec - from hot to cold in 1 seconds',
            ],
            defaultValue: '20',
            required: true
        )
        
        // Inputs for capability.Brightness
        input(
            name: 'levelStep', type: 'enum',
            title: 'Brightness up/down shift',
            description: '<small>Brightness +/- adjust for the shiftLevel() command.</small>',
            options: ['1':'1%', '2':'2%', '5':'5%', '10':'10%', '20':'20%', '25':'25%', '33':'33%', '50':'50%'],
            defaultValue: '25',
            required: true
        )
        input(
            name: 'levelChangeRate', type: 'enum',
            title: 'Brightness change rate',
            description: '<small>Brightness +/- adjust for the startLevelChange() command.</small>',
            options: [
                 '10': '10% / sec - from 0% to 100% in 10 seconds',
                 '20': '20% / sec - from 0% to 100% in 5 seconds',
                 '33': '33% / sec - from 0% to 100% in 3 seconds',
                 '50': '50% / secs - from 0% to 100% in 2 seconds',
                '100': '100% / sec - from 0% to 100% in 1 seconds',
            ],
            defaultValue: '20',
            required: true
        )
        input(
            name: 'transitionTime', type: 'enum',
            title: 'Brightness transition time',
            description: '<small>Time taken to move to/from the target brightness when device is turned On/Off.</small>',
            options: [
                 '0': 'Instant',
                 '5': '0.5 seconds',
                '10': '1 second',
                '15': '1.5 seconds',
                '20': '2 seconds',
                '30': '3 seconds',
                '40': '4 seconds',
                '50': '5 seconds',
               '100': '10 seconds'
            ],
            defaultValue: '5',
            required: true
        )
        input(
            name: 'turnOnBehavior', type: 'enum',
            title: 'Turn On behavior',
            description: '<small>Select what happens when the device is turned On.</small>',
            options: [
                'RESTORE_PREVIOUS_LEVEL': 'Restore previous brightness',
                'FIXED_VALUE': 'Always start with the same fixed brightness'
            ],
            defaultValue: 'RESTORE_PREVIOUS_LEVEL',
            required: true
        )
        if (turnOnBehavior == 'FIXED_VALUE') {
            input(
                name: 'onLevelValue',
                type: 'number',
                title: 'Fixed brightness value',
                description: '<small>Range 1..100</small>',
                defaultValue: 50,
                range: '1..100',
                required: true
            )
        }
        input(
            name: 'prestaging', type: 'bool',
            title: 'Pre-staging',
            description: '<small>Set brightness level without turning On the device (for later use).</small>',
            defaultValue: false,
            required: true
        )
        
        // Inputs for capability.ZigbeeBindings
        input(
            name: 'joinGroup', type: 'enum',
            title: 'Join a Zigbee group',
            description: '<small>Select a Zigbee group you want to join.</small>',
            options: ['0000':'❌ Leave all Zigbee groups', '----':'- - - -'] + GROUPS,
            defaultValue: '----',
            required: false
        )
    }
}

// ===================================================================================================================
// Implement default methods
// ===================================================================================================================

// Called when the device is first added
void installed() {
    log_warn 'Installing device ...'
    log_warn '[IMPORTANT] For battery-powered devices, make sure that you keep your device as close as you can (less than 2inch / 5cm) to your Hubitat hub for at least 30 seconds. Otherwise the device will successfully pair but it won\'t work properly!'
}

// Called when the "Save Preferences" button is clicked
List<String> updated(boolean auto = false) {
    log_info "Saving preferences${auto ? ' (auto)' : ''} ..."
    List<String> cmds = []

    unschedule()

    if (logLevel == null) {
        logLevel = '1'
        device.updateSetting 'logLevel', [value:logLevel, type:'enum']
    }
    if (logLevel == '1') runIn 1800, 'logsOff'
    log_info "🛠️ logLevel = ${['1':'Debug', '2':'Info', '3':'Warning', '4':'Error'].get(logLevel)}"
    
    // Preferences for capability.Switch
    if (powerOnBehavior == null) {
        powerOnBehavior = 'RESTORE_PREVIOUS_STATE'
        device.updateSetting 'powerOnBehavior', [value:powerOnBehavior, type:'enum']
    }
    log_info "🛠️ powerOnBehavior = ${powerOnBehavior}"
    cmds += zigbee.writeAttribute(0x0006, 0x4003, 0x30, powerOnBehavior == 'TURN_POWER_OFF' ? 0x00 : (powerOnBehavior == 'TURN_POWER_ON' ? 0x01 : 0xFF))
    
    // Preferences for capability.ColorTemperature
    if (colorTemperatureStep == null) {
        colorTemperatureStep = '20'
        device.updateSetting 'colorTemperatureStep', [value:colorTemperatureStep, type:'enum']
    }
    log_info "🛠️ colorTemperatureStep = ${colorTemperatureStep}%"
    
    if (colorTemperatureChangeRate == null) {
        colorTemperatureChangeRate = '20'
        device.updateSetting 'colorTemperatureChangeRate', [value:colorTemperatureChangeRate, type:'enum']
    }
    log_info "🛠️ colorTemperatureChangeRate = ${colorTemperatureChangeRate}% / second"
    
    // Regardless of prestaging, enable update of color temperature without the need for the device to be turned On
    cmds += zigbee.writeAttribute(0x0300, 0x000F, 0x18, 0x01)
    
    // Preferences for capability.Brightness
    if (levelStep == null) {
        levelStep = '20'
        device.updateSetting 'levelStep', [value:levelStep, type:'enum']
    }
    log_info "🛠️ levelStep = ${levelStep}%"
    
    if (levelChangeRate == null) {
        levelChangeRate = '20'
        device.updateSetting 'levelChangeRate', [value:levelChangeRate, type:'enum']
    }
    log_info "🛠️ levelChangeRate = ${levelChangeRate}% / second"
    
    if (turnOnBehavior == null) {
        turnOnBehavior = 'RESTORE_PREVIOUS_LEVEL'
        device.updateSetting 'turnOnBehavior', [value:turnOnBehavior, type:'enum']
    }
    log_info "🛠️ turnOnBehavior = ${turnOnBehavior}"
    if (turnOnBehavior == 'FIXED_VALUE') {
        Integer onLevelValue = onLevelValue == null ? 50 : onLevelValue.intValue()
        device.updateSetting 'onLevelValue', [value:onLevelValue, type:'number']
        log_info "🛠️ onLevelValue = ${onLevelValue}%"
        Integer lvl = onLevelValue * 2.54
        utils_sendZigbeeCommands zigbee.writeAttribute(0x0008, 0x0011, 0x20, lvl)
    } else {
        log_debug 'Disabling OnLevel (0xFF)'
        cmds += zigbee.writeAttribute(0x0008, 0x0011, 0x20, 0xFF)
    }
    
    if (transitionTime == null) {
        transitionTime = '5'
        device.updateSetting 'transitionTime', [value:transitionTime, type:'enum']
    }
    log_info "🛠️ transitionTime = ${Integer.parseInt(transitionTime) / 10} second(s)"
    cmds += zigbee.writeAttribute(0x0008, 0x0010, 0x21, Integer.parseInt(transitionTime))
    
    if (prestaging == null) {
        prestaging = false
        device.updateSetting 'prestaging', [value:prestaging, type:'bool']
    }
    log_info "🛠️ prestaging = ${prestaging}"
    
    // If prestaging is true, enable update of brightness without the need for the device to be turned On
    cmds += zigbee.writeAttribute(0x0008, 0x000F, 0x18, prestaging ? 0x01 : 0x00)
    
    // Preferences for capability.HealthCheck
    schedule HEALTH_CHECK.schedule, 'healthCheck'
    
    // Preferences for capability.ZigbeeGroups
    if (joinGroup != null && joinGroup != '----') {
        if (joinGroup == '0000') {
            log_info '🛠️ Leaving all Zigbee groups'
            cmds += "he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0004 {0143 04}" // Leave all groups
        } else {
            String groupName = GROUPS.getOrDefault(joinGroup, 'Unknown')
            log_info "🛠️ Joining group: ${joinGroup} (${groupName})"
            cmds += "he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0004 {0143 00 ${utils_payload joinGroup} ${Integer.toHexString(groupName.length()).padLeft(2, '0')}${groupName.bytes.encodeHex()}}"  // Join group
        }
        device.updateSetting 'joinGroup', [value:'----', type:'enum']
        cmds += "he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0004 {0143 02 00}" // Get groups membership
    }

    if (auto) return cmds
    utils_sendZigbeeCommands cmds
    return []
}

// ===================================================================================================================
// Capabilities helpers
// ===================================================================================================================

// Handler method for scheduled job to disable debug logging
void logsOff() {
    log_info '⏲️ Automatically reverting log level to "Info"'
    device.updateSetting 'logLevel', [value:'2', type:'enum']
}

// Helpers for capability.HealthCheck
void healthCheck() {
    log_debug '⏲️ Automatically running health check'
    String healthStatus = state.lastRx == 0 || state.lastRx == null ? 'unknown' : (now() - state.lastRx < Integer.parseInt(HEALTH_CHECK.thereshold) * 1000 ? 'online' : 'offline')
    utils_sendEvent name:'healthStatus', value:healthStatus, type:'physical', descriptionText:"Health status is ${healthStatus}"
}

// ===================================================================================================================
// Implement Capabilities
// ===================================================================================================================

// capability.Configuration
// Note: This method is also called when the device is initially installed
void configure(boolean auto = false) {
    log_warn "Configuring device${auto ? ' (auto)' : ''} ..."
    if (!auto && device.currentValue('powerSource', true) == 'battery') {
        log_warn '[IMPORTANT] Click the "Configure" button immediately after pushing any button on the device in order to first wake it up!'
    }

    // Apply preferences first
    List<String> cmds = []
    cmds += updated true

    // Clear data (keep firmwareMT information though)
    device.data*.key.each { if (it != 'firmwareMT') device.removeDataValue it }

    // Clear state
    state.clear()
    state.lastTx = 0
    state.lastRx = 0
    state.lastCx = DRIVER_VERSION
    
    // Configuration for capability.Switch
    cmds += "zdo bind 0x${device.deviceNetworkId} 0x${device.endpointId} 0x01 0x0006 {${device.zigbeeId}} {}" // On/Off cluster
    cmds += "he cr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0006 0x0000 0x10 0x0000 0x0258 {01} {}" // Report OnOff (bool) at least every 10 minutes
    
    // Configuration for capability.ColorTemperature
    cmds += "zdo bind 0x${device.deviceNetworkId} 0x${device.endpointId} 0x01 0x0300 {${device.zigbeeId}} {}" // Color Control Cluster cluster
    cmds += "he cr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0300 0x0007 0x21 0x0000 0x0258 {01} {}" // Report ColorTemperatureMireds (uint16) at least every 10 minutes (Δ = 1)
    state.minMireds = 200  // Will be updated in refresh()
    state.maxMireds = 600  // Will be updated in refresh()
    
    // Configuration for capability.Brightness
    cmds += "zdo bind 0x${device.deviceNetworkId} 0x${device.endpointId} 0x01 0x0008 {${device.zigbeeId}} {}" // Level Control cluster
    cmds += "he cr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0008 0x0000 0x20 0x0000 0x0258 {01} {}" // Report CurrentLevel (uint8) at least every 10 minutes (Δ = 1)
    
    // Configuration for capability.HealthCheck
    sendEvent name:'healthStatus', value:'online', descriptionText:'Health status initialized to online'
    sendEvent name:'checkInterval', value:3600, unit:'second', descriptionText:'Health check interval is 3600 seconds'
    
    // Configuration for capability.PowerSource
    sendEvent name:'powerSource', value:'unknown', type:'digital', descriptionText:'Power source initialized to unknown'
    cmds += zigbee.readAttribute(0x0000, 0x0007) // PowerSource

    // Query Basic cluster attributes
    cmds += zigbee.readAttribute(0x0000, [0x0001, 0x0003, 0x0004, 0x4000]) // ApplicationVersion, HWVersion, ManufacturerName, SWBuildID
    cmds += zigbee.readAttribute(0x0000, [0x0005]) // ModelIdentifier
    cmds += zigbee.readAttribute(0x0000, [0x000A]) // ProductCode
    utils_sendZigbeeCommands cmds

    log_info 'Configuration done; refreshing device current state in 7 seconds ...'
    runIn 7, 'refresh', [data:true]
}
private void autoConfigure() {
    log_warn "Detected that this device is not properly configured for this driver version (lastCx != ${DRIVER_VERSION})"
    configure true
}

// capability.Refresh
void refresh(boolean auto = false) {
    log_warn "Refreshing device state${auto ? ' (auto)' : ''} ..."
    if (!auto && device.currentValue('powerSource', true) == 'battery') {
        log_warn '[IMPORTANT] Click the "Refresh" button immediately after pushing any button on the device in order to first wake it up!'
    }

    List<String> cmds = []
    
    // Refresh for capability.Switch
    cmds += zigbee.readAttribute(0x0006, 0x0000) // OnOff
    cmds += zigbee.readAttribute(0x0006, 0x4003) // PowerOnBehavior
    
    // Refresh for capability.ColorTemperature
    cmds += zigbee.readAttribute(0x0300, [0x0007, 0x0008]) // ColorTemperatureMireds, ColorMode
    cmds += zigbee.readAttribute(0x0300, [0x400B, 0x400C]) // ColorTemperaturePhysicalMinMireds, ColorTemperaturePhysicalMaxMireds
    
    // Refresh for capability.Brightness
    cmds += zigbee.readAttribute(0x0008, 0x0000) // CurrentLevel
    
    // Refresh for capability.ZigbeeGroups
    cmds += "he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0004 {0143 02 00}" // Get groups membership
    utils_sendZigbeeCommands cmds
}

// Implementation for capability.Switch
void on() {
    log_debug 'Sending On command'
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0006 {114301}"])
}
void off() {
    log_debug 'Sending Off command'
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0006 {114300}"])
}

void toggle() {
    log_debug 'Sending Toggle command'
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0006 {114302}"])
}

void onWithTimedOff(BigDecimal onTime = 1) {
    Integer delay = onTime < 1 ? 1 : (onTime > 6500 ? 6500 : onTime)
    log_debug 'Sending OnWithTimedOff command'
    Integer dur = delay * 10
    String payload = "00 ${utils_payload dur, 4} 0000"
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0006 {114342 ${payload}}"])
}

// Implementation for capability.ColorTemperature
void setColorTemperature(BigDecimal colorTemperature, BigDecimal level = -1, BigDecimal duration = 0) {
    Integer mireds = Math.round(1000000 / colorTemperature)
    mireds = mireds < state.minMireds ? state.minMireds : (mireds > state.maxMireds ? state.maxMireds : mireds)
    Integer newColorTemperature = Math.round(1000000 / mireds)
    log_debug "Setting color temperature to ${newColorTemperature}k (${mireds} mireds) during ${duration} seconds"
    Integer dur = (duration > 1800 ? 1800 : (duration < 0 ? 0 : duration)) * 10 // Max transition time = 30 min
    String payload = "${utils_payload mireds, 4} ${utils_payload dur, 4}"
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0300 {11430A ${payload}}"])
    if (level > 0 && duration == 0) setLevel level, duration
}
void startColorTemperatureChange(String direction) {
    log_debug "Starting color temperature change ${direction}wards with a rate of ${colorTemperatureChangeRate}% / second"
    Integer mode = direction == 'up' ? 0x03 : 0x01
    Integer changeRate = (state.maxMireds - state.minMireds) * Integer.parseInt(colorTemperatureChangeRate) / 100
    String payload = "${utils_payload mode, 2} ${utils_payload changeRate, 4} ${utils_payload state.minMireds, 4} ${utils_payload state.maxMireds, 4} 00 00"
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0300 {11434B ${payload}}"])
}
void stopColorTemperatureChange() {
    log_debug 'Stopping color temperature change'
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0300 {114347 00 00}"])
}
void shiftColorTemperature(String direction) {
    log_debug "Shifting color temperature ${direction} by ${colorTemperatureStep}%"
    Integer mode = direction == 'up' ? 0x03 : 0x01
    Integer stepSize = (state.maxMireds - state.minMireds) * Integer.parseInt(colorTemperatureStep) / 100
    String payload = "${utils_payload mode, 2} ${utils_payload stepSize, 4} 0000 ${utils_payload state.minMireds, 4} ${utils_payload state.maxMireds, 4} 00 00"
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0300 {11434C ${payload}}"])
}
private void processMultipleColorTemperatureAttributes(Map msg, String type) {
    Map<Integer, String> attributes = [:]
    attributes[msg.attrInt] = msg.value
    msg.additionalAttrs?.each { attributes[Integer.parseInt(it.attrId, 16)] = it.value }

    Integer mireds = -1
    Integer temperature = -1
    String colorMode = null
    String colorName = null
    attributes.each {
        switch (it.key) {
            case 0x0007:
                mireds = Integer.parseInt it.value, 16
                temperature = Math.round(1000000 / mireds)
                break
            case 0x0008:
            case 0x4001:
                colorMode = it.value == '02' ? 'CT' : 'RGB'
                utils_sendEvent name:'colorMode', value:colorMode, descriptionText:"Color mode is ${colorMode}", type:type
                break
        }
    }

    if (temperature >= 0) utils_sendEvent name:'colorTemperature', value:temperature, descriptionText:"Color temperature is ${temperature}K", type:type

    // Update colorName, if the case
    if ("${colorMode ?: device.currentValue('colorMode', true)}" == 'CT') {
        Integer colorTemperature = temperature >= 0 ? temperature : device.currentValue('colorTemperature', true)
        colorName = convertTemperatureToGenericColorName colorTemperature
        utils_sendEvent name:'colorName', value:colorName, descriptionText:"Color name is ${colorName}", type:type
    }

    utils_processedZclMessage "${msg.commandInt == 0x0A ? 'Report' : 'Read'} Attributes Response", "ColorTemperatureMireds=${mireds} (${temperature}K), ${colorName}), ColorMode=${colorMode}"
}

// Implementation for capability.Brightness
void setLevel(BigDecimal level, BigDecimal duration = 0) {
    Integer newLevel = level > 100 ? 100 : (level < 0 ? 0 : level)
    log_debug "Setting brightness level to ${newLevel}% during ${duration} seconds"
    Integer lvl = newLevel * 2.54
    Integer dur = (duration > 1800 ? 1800 : (duration < 0 ? 0 : duration)) * 10 // Max transition time = 30 min
    String command = prestaging == false ? '04' : '00'
    String payload = "${utils_payload lvl, 2} ${utils_payload dur, 4}"
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0008 {1143${command} ${payload}}"])
}
void startLevelChange(String direction) {
    log_debug "Starting brightness level change ${direction}wards with a rate of ${levelChangeRate}% / second"
    Integer mode = direction == 'up' ? 0x00 : 0x01
    Integer rate = Integer.parseInt(levelChangeRate) * 2.54
    String payload = "${utils_payload mode, 2} ${utils_payload rate, 2}"
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0008 {114301 ${payload}}"])
}
void stopLevelChange() {
    log_debug 'Stopping brightness level change'
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0008 {114303}"])
}
void shiftLevel(String direction) {
    log_debug "Shifting brightness level ${direction} by ${levelStep}%"
    Integer mode = direction == 'up' ? 0x00 : 0x01
    Integer stepSize = Integer.parseInt(levelStep) * 2.54
    String payload = "${utils_payload mode, 2} ${utils_payload stepSize, 2} 0000"
    utils_sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x${device.endpointId} 0x0008 {114302 ${payload}}"])
}

// Implementation for capability.HealthCheck
void ping() {
    log_warn 'ping ...'
    utils_sendZigbeeCommands(zigbee.readAttribute(0x0000, 0x0000))
    log_debug 'Ping command sent to the device; we\'ll wait 5 seconds for a reply ...'
    runIn 5, 'pingExecute'
}
void pingExecute() {
    if (state.lastRx == 0) {
        log_info 'Did not sent any messages since it was last configured'
        return
    }

    Date now = new Date(Math.round(now() / 1000) * 1000)
    Date lastRx = new Date(Math.round(state.lastRx / 1000) * 1000)
    String lastRxAgo = TimeCategory.minus(now, lastRx).toString().replace('.000 seconds', ' seconds')
    log_info "Sent last message at ${lastRx.format('yyyy-MM-dd HH:mm:ss', location.timeZone)} (${lastRxAgo} ago)"

    Date thereshold = new Date(Math.round(state.lastRx / 1000 + Integer.parseInt(HEALTH_CHECK.thereshold)) * 1000)
    String theresholdAgo = TimeCategory.minus(thereshold, lastRx).toString().replace('.000 seconds', ' seconds')
    log_info "Will be marked as offline if no message is received for ${theresholdAgo} (hardcoded)"

    String offlineMarkAgo = TimeCategory.minus(thereshold, now).toString().replace('.000 seconds', ' seconds')
    log_info "Will be marked as offline if no message is received until ${thereshold.format('yyyy-MM-dd HH:mm:ss', location.timeZone)} (${offlineMarkAgo} from now)"
}

// Implementation for capability.FirmwareUpdate
void updateFirmware() {
    log_info 'Looking for firmware updates ...'
    if (device.currentValue('powerSource', true) == 'battery') {
        log_warn '[IMPORTANT] Click the "Update Firmware" button immediately after pushing any button on the device in order to first wake it up!'
    }
    utils_sendZigbeeCommands zigbee.updateFirmware()
}

// ===================================================================================================================
// Handle incoming Zigbee messages
// ===================================================================================================================

void parse(String description) {
    log_debug "description=[${description}]"

    // Auto-Configure device: configure() was not called for this driver version
    if (state.lastCx != DRIVER_VERSION) {
        state.lastCx = DRIVER_VERSION
        runInMillis 1500, 'autoConfigure'
    }

    // Extract msg
    Map msg = [:]
    if (description.startsWith('zone status')) msg += [clusterInt:0x500, commandInt:0x00, isClusterSpecific:true]
    if (description.startsWith('enroll request')) msg += [clusterInt:0x500, commandInt:0x01, isClusterSpecific:true]

    msg += zigbee.parseDescriptionAsMap description
    if (msg.containsKey('endpoint')) msg.endpointInt = Integer.parseInt(msg.endpoint, 16)
    if (msg.containsKey('sourceEndpoint')) msg.endpointInt = Integer.parseInt(msg.sourceEndpoint, 16)
    if (msg.containsKey('cluster')) msg.clusterInt = Integer.parseInt(msg.cluster, 16)
    if (msg.containsKey('command')) msg.commandInt = Integer.parseInt(msg.command, 16)
    log_debug "msg=[${msg}]"

    state.lastRx = now()
    
    // Parse for capability.HealthCheck
    if (device.currentValue('healthStatus', true) != 'online') {
        utils_sendEvent name:'healthStatus', value:'online', type:'digital', descriptionText:'Health status changed to online'
    }

    // If we sent a Zigbee command in the last 3 seconds, we assume that this Zigbee event is a consequence of this driver doing something
    // Therefore, we mark this event as "digital"
    String type = state.containsKey('lastTx') && (now() - state.lastTx < 3000) ? 'digital' : 'physical'

    switch (msg) {
        
        // Events for capability.Switch
        // ===================================================================================================================
        
        // Report/Read Attributes: OnOff
        case { contains it, [clusterInt:0x0006, commandInt:0x0A, attrInt:0x0000] }:
        case { contains it, [clusterInt:0x0006, commandInt:0x01, attrInt:0x0000] }:
            String newState = msg.value == '00' ? 'off' : 'on'
            utils_sendEvent name:'switch', value:newState, descriptionText:"Was turned ${newState}", type:type
        
            utils_processedZclMessage "${msg.commandInt == 0x0A ? 'Report' : 'Read'} Attributes Response", "OnOff=${newState}"
            return
        
        // Read Attributes Response: powerOnBehavior
        case { contains it, [clusterInt:0x0006, commandInt:0x01, attrInt:0x4003] }:
            String newValue = ''
            switch (Integer.parseInt(msg.value, 16)) {
                case 0x00: newValue = 'TURN_POWER_OFF'; break
                case 0x01: newValue = 'TURN_POWER_ON'; break
                case 0xFF: newValue = 'RESTORE_PREVIOUS_STATE'; break
                default: log_warn "Received unexpected attribute value: PowerOnBehavior=${msg.value}"; return
            }
            powerOnBehavior = newValue
            device.updateSetting 'powerOnBehavior', [value:newValue, type:'enum']
            utils_processedZclMessage 'Read Attributes Response', "PowerOnBehavior=${newValue}"
            return
        
        // Other events that we expect but are not usefull for capability.Switch behavior
        case { contains it, [clusterInt:0x0006, commandInt:0x07] }:
            utils_processedZclMessage 'Configure Reporting Response', "attribute=OnOff, data=${msg.data}"
            return
        case { contains it, [clusterInt:0x0006, commandInt:0x04] }: // Write Attribute Response
        case { contains it, [clusterInt:0x0006, commandInt:0x06, isClusterSpecific:false, direction:'01'] }: // Configure Reporting Command
            return
        
        // Events for capability.ColorTemperature
        // ===================================================================================================================
        
        // Report/Read Attributes Reponse: ColorTemperatureMireds
        case { contains it, [clusterInt:0x0300, commandInt:0x0A, attrInt:0x0007] }:
        case { contains it, [clusterInt:0x0300, commandInt:0x01, attrInt:0x0007] }:
        
        // Report/Read Attributes Reponse: ColorMode
        case { contains it, [clusterInt:0x0300, commandInt:0x0A, attrInt:0x0008] }:
        case { contains it, [clusterInt:0x0300, commandInt:0x01, attrInt:0x0008] }:
        
        // Report/Read Attributes Reponse: EnhancedColorMode
        case { contains it, [clusterInt:0x0300, commandInt:0x0A, attrInt:0x4001] }:
        case { contains it, [clusterInt:0x0300, commandInt:0x01, attrInt:0x4001] }:
            processMultipleColorTemperatureAttributes msg, type
            return
        
        // Read Attributes Reponse: ColorTemperaturePhysicalMinMireds, ColorTemperaturePhysicalMaxMireds
        case { contains it, [clusterInt:0x0300, commandInt:0x01, attrInt:0x400B] }:
            state.minMireds = Integer.parseInt msg.value, 16
            msg.additionalAttrs?.each { if (it.attrId == '400C') state.maxMireds = Integer.parseInt it.value, 16 }
            utils_processedZclMessage 'Read Attributes Response', "ColorTemperaturePhysicalMinMireds=${msg.value} (${state.minMireds} mireds, ${Math.round(1000000 / state.minMireds)}K), ColorTemperaturePhysicalMaxMireds=${msg.value} (${state.maxMireds} mireds, ${Math.round(1000000 / state.maxMireds)}K)"
            return
        
        // Other events that we expect but are not usefull for capability.ColorTemperature behavior
        case { contains it, [clusterInt:0x0300, commandInt:0x07] }:
            utils_processedZclMessage 'Configure Reporting Response', "attribute=ColorTemperatureMireds, data=${msg.data}"
            return
        case { contains it, [clusterInt:0x0300, commandInt:0x04] }: // Write Attribute Response (0x04)
            return
        
        // Events for capability.Brightness
        // ===================================================================================================================
        
        // Report/Read Attributes Reponse: CurrentLevel
        case { contains it, [clusterInt:0x0008, commandInt:0x0A, attrInt:0x0000] }:
        case { contains it, [clusterInt:0x0008, commandInt:0x01, attrInt:0x0000] }:
            Integer level = msg.value == '00' ? 0 : Math.ceil(Integer.parseInt(msg.value, 16) / 2.54)
            utils_sendEvent name:'level', value:level, descriptionText:"Brightness is ${level}%", type:'digital'
            utils_processedZclMessage "${msg.commandInt == 0x0A ? 'Report' : 'Read'} Attributes Response", "CurrentLevel=${msg.value} (${level}%)"
            return
        
        // Other events that we expect but are not usefull for capability.Brightness behavior
        case { contains it, [clusterInt:0x0008, commandInt:0x07] }:
            utils_processedZclMessage 'Configure Reporting Response', "attribute=CurrentLevel, data=${msg.data}"
            return
        case { contains it, [clusterInt:0x0008, commandInt:0x04] }: // Write Attribute Response (0x04)
            return
        
        // Events for capability.HealthCheck
        // ===================================================================================================================
        
        case { contains it, [clusterInt:0x0000, attrInt:0x0000] }:
            log_warn '... pong'
            return
        
        // Configuration for capability.PowerSource
        // ===================================================================================================================
        
        // Read Attributes Reponse: PowerSource
        case { contains it, [clusterInt:0x0000, commandInt:0x01, attrInt:0x0007] }:
            String powerSource = 'unknown'
        
            // PowerSource := { 0x00:Unknown, 0x01:MainsSinglePhase, 0x02:MainsThreePhase, 0x03:Battery, 0x04:DC, 0x05:EmergencyMainsConstantlyPowered, 0x06:EmergencyMainsAndTransferSwitch }
            switch (msg.value) {
                case ['01', '02', '05', '06']:
                    powerSource = 'mains'; break
                case '03':
                    powerSource = 'battery'; break
                case '04':
                    powerSource = 'dc'
            }
            utils_sendEvent name:'powerSource', value:powerSource, type:'digital', descriptionText:"Power source is ${powerSource}"
            utils_processedZclMessage 'Read Attributes Response', "PowerSource=${msg.value}"
            return
        
        // Events for capability.ZigbeeGroups
        // ===================================================================================================================
        
        // Get Group Membership Response Command
        case { contains it, [clusterInt:0x0004, commandInt:0x02, direction:'01'] }:
            Integer count = Integer.parseInt msg.data[1], 16
            Set<String> groupNames = []
            for (int pos = 0; pos < count; pos++) {
                String groupId = "${msg.data[pos * 2 + 3]}${msg.data[pos * 2 + 2]}"
                String groupName = GROUPS.containsKey(groupId) ? "<abbr title=\"0x${groupId}\">${GROUPS.get(groupId)}</abbr>" : "0x${groupId}"
                log_debug "Found group membership: ${groupName}"
                groupNames.add groupName
            }
            state.joinGrp = groupNames
            if (state.joinGrp.size() == 0) state.remove 'joinGrp'
            log_info "Current group membership: ${groupNames ?: 'None'}"
            return
        
        // Add Group Response
        case { contains it, [clusterInt:0x0004, commandInt:0x00, direction:'01'] }:
            String status = msg.data[0] == '00' ? 'SUCCESS' : (msg.data[0] == '8A' ? 'ALREADY_MEMBER' : 'FAILED')
            String groupId = "${msg.data[2]}${msg.data[1]}"
            String groupName = GROUPS.containsKey(groupId) ? "<abbr title=\"0x${groupId}\">${GROUPS.get(groupId)}</abbr>" : "0x${groupId}"
            utils_processedZclMessage 'Add Group Response', "Status=${status}, groupId=${groupId}, groupName=${groupName}"
            return
        
        // Leave Group Response
        case { contains it, [clusterInt:0x0004, commandInt:0x03, direction:'01'] }:
            String status = msg.data[0] == '00' ? 'SUCCESS' : (msg.data[0] == '8B' ? 'NOT_A_MEMBER' : 'FAILED')
            String groupId = "${msg.data[2]}${msg.data[1]}"
            String groupName = GROUPS.containsKey(groupId) ? "<abbr title=\"0x${groupId}\">${GROUPS.get(groupId)}</abbr>" : "0x${groupId}"
            utils_processedZclMessage 'Left Group Response', "Status=${status}, groupId=${groupId}, groupName=${groupName}"
            return

        // ---------------------------------------------------------------------------------------------------------------
        // Handle common messages (e.g.: received during pairing when we query the device for information)
        // ---------------------------------------------------------------------------------------------------------------

        // Device_annce: Welcome back! let's sync state.
        case { contains it, [endpointInt:0x00, clusterInt:0x0013, commandInt:0x00] }:
            log_warn 'Rejoined the Zigbee mesh; refreshing device state in 3 seconds ...'
            runIn 3, 'refresh'
            return

        // Report/Read Attributes Response (Basic cluster)
        case { contains it, [clusterInt:0x0000, commandInt:0x01] }:
        case { contains it, [clusterInt:0x0000, commandInt:0x0A] }:
            utils_zigbeeDataValue(msg.attrInt, msg.value)
            msg.additionalAttrs?.each { utils_zigbeeDataValue(it.attrInt, it.value) }
            utils_processedZclMessage "${msg.commandInt == 0x0A ? 'Report' : 'Read'} Attributes Response", "cluster=0x${msg.cluster}, attribute=0x${msg.attrId}, value=${msg.value}"
            return

        // Mgmt_Leave_rsp
        case { contains it, [endpointInt:0x00, clusterInt:0x8034, commandInt:0x00] }:
            log_warn 'Device is leaving the Zigbee mesh. See you later, Aligator!'
            return

        // Ignore the following Zigbee messages
        case { contains it, [commandInt:0x0A, isClusterSpecific:false] }:              // ZCL: Attribute report we don't care about (configured by other driver)
        case { contains it, [commandInt:0x0B, isClusterSpecific:false] }:              // ZCL: Default Response
        case { contains it, [clusterInt:0x0003, commandInt:0x01] }:                    // ZCL: Identify Query Command
            utils_processedZclMessage 'Ignored', "endpoint=${msg.endpoint}, cluster=0x${msg.clusterId}, command=0x${msg.command}, data=${msg.data}"
            return

        case { contains it, [endpointInt:0x00, clusterInt:0x8001, commandInt:0x00] }:  // ZDP: IEEE_addr_rsp
        case { contains it, [endpointInt:0x00, clusterInt:0x8004, commandInt:0x00] }:  // ZDP: Simple_Desc_rsp
        case { contains it, [endpointInt:0x00, clusterInt:0x8005, commandInt:0x00] }:  // ZDP: Active_EP_rsp
        case { contains it, [endpointInt:0x00, clusterInt:0x0006, commandInt:0x00] }:  // ZDP: MatchDescriptorRequest
        case { contains it, [endpointInt:0x00, clusterInt:0x801F, commandInt:0x00] }:  // ZDP: Parent_annce_rsp
        case { contains it, [endpointInt:0x00, clusterInt:0x8021, commandInt:0x00] }:  // ZDP: Mgmt_Bind_rsp
        case { contains it, [endpointInt:0x00, clusterInt:0x8022, commandInt:0x00] }:  // ZDP: Mgmt_Unbind_rsp
        case { contains it, [endpointInt:0x00, clusterInt:0x8031, commandInt:0x00] }:  // ZDP: Mgmt_LQI_rsp
        case { contains it, [endpointInt:0x00, clusterInt:0x8032, commandInt:0x00] }:  // ZDP: Mgmt_Rtg_rsp
        case { contains it, [endpointInt:0x00, clusterInt:0x8038, commandInt:0x00] }:  // ZDP: Mgmt_NWK_Update_notify
            utils_processedZdpMessage 'Ignored', "cluster=0x${msg.clusterId}, command=0x${msg.command}, data=${msg.data}"
            return

        // ---------------------------------------------------------------------------------------------------------------
        // Unexpected Zigbee message
        // ---------------------------------------------------------------------------------------------------------------
        default:
            log_error "Sent unexpected Zigbee message: description=${description}, msg=${msg}"
    }
}

// ===================================================================================================================
// Logging helpers (something like this should be part of the SDK and not implemented by each driver)
// ===================================================================================================================

private void log_debug(String message) {
    if (logLevel == '1') log.debug "${device.displayName} ${message.uncapitalize()}"
}
private void log_info(String message) {
    if (logLevel <= '2') log.info "${device.displayName} ${message.uncapitalize()}"
}
private void log_warn(String message) {
    if (logLevel <= '3') log.warn "${device.displayName} ${message.uncapitalize()}"
}
private void log_error(String message) {
    log.error "${device.displayName} ${message.uncapitalize()}"
}

// ===================================================================================================================
// Helper methods (keep them simple, keep them dumb)
// ===================================================================================================================

private void utils_sendZigbeeCommands(List<String> cmds) {
    if (cmds.empty) return
    List<String> send = delayBetween(cmds.findAll { !it.startsWith('delay') }, 1000)
    log_debug "◀ Sending Zigbee messages: ${send}"
    state.lastTx = now()
    sendHubCommand new hubitat.device.HubMultiAction(send, hubitat.device.Protocol.ZIGBEE)
}
private void utils_sendEvent(Map event) {
    if (device.currentValue(event.name, true) != event.value || event.isStateChange) {
        log_info "${event.descriptionText} [${event.type}]"
    } else {
        log_debug "${event.descriptionText} [${event.type}]"
    }
    sendEvent event
}
private void utils_dataValue(String key, String value) {
    if (value == null || value == '') return
    log_debug "Update data value: ${key}=${value}"
    updateDataValue key, value
}
private void utils_zigbeeDataValue(Integer attrInt, String value) {
    switch (attrInt) {
        case 0x0001: utils_dataValue 'application', value; return
        case 0x0003: utils_dataValue 'hwVersion', value; return
        case 0x0004: utils_dataValue 'manufacturer', value; return
        case 0x000A: utils_dataValue 'type', "${value ? (value.split('') as List).collate(2).collect { "${Integer.parseInt(it.join(), 16) as char}" }.join() : ''}"; return
        case 0x0005: utils_dataValue 'model', value; return
        case 0x4000: utils_dataValue 'softwareBuild', value; return
    }
}
private void utils_processedZclMessage(String type, String details) {
    log_debug "▶ Processed ZCL message: type=${type}, ${details}"
}
private void utils_processedZdpMessage(String type, String details) {
    log_debug "▶ Processed ZDO message: type=${type}, ${details}"
}
private String utils_payload(String value) {
    return value.replace('0x', '').split('(?<=\\G.{2})').reverse().join('')
}
private String utils_payload(Integer value, Integer size = 4) {
    return utils_payload(Integer.toHexString(value).padLeft(size, '0'))
}

// switch/case syntactic sugar
@CompileStatic private boolean contains(Map msg, Map spec) {
    return msg.keySet().containsAll(spec.keySet()) && spec.every { it.value == msg[it.key] }
}
