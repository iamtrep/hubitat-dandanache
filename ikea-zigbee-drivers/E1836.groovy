/**
 * IKEA Askvader On/Off Switch (E1836)
 *
 * @see https://dan-danache.github.io/hubitat/ikea-zigbee-drivers/
 * @see https://zigbee.blakadder.com/Ikea_E1836.html
 * @see https://ww8.ikea.com/ikeahomesmart/releasenotes/releasenotes.html
 * @see https://static.homesmart.ikea.com/releaseNotes/
 */
import groovy.time.TimeCategory
import groovy.transform.Field

@Field static final String DRIVER_NAME = "IKEA Askvader On/Off Switch (E1836)"
@Field static final String DRIVER_VERSION = "3.9.0"

// Fields for capability.HealthCheck
@Field static final Map<String, String> HEALTH_CHECK = [
    "schedule": "0 0 0/1 ? * * *", // Health will be checked using this cron schedule
    "thereshold": "3600" // When checking, mark the device as offline if no Zigbee message was received in the last 3600 seconds
]

metadata {
    definition(name:DRIVER_NAME, namespace:"dandanache", author:"Dan Danache", importUrl:"https://raw.githubusercontent.com/dan-danache/hubitat/master/ikea-zigbee-drivers/E1836.groovy") {
        capability "Configuration"
        capability "Actuator"
        capability "Switch"
        capability "HealthCheck"
        capability "PowerSource"
        capability "Refresh"

        // For firmware: 1.0.002 (117C-110D-00010002)
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0003,0004,0005,0006,0008,1000,FC57", outClusters:"0019", model:"ASKVADER on/off switch", manufacturer:"IKEA of Sweden"
        
        // Attributes for capability.HealthCheck
        attribute "healthStatus", "enum", ["offline", "online", "unknown"]
    }
    
    // Commands for capability.Switch
    command "toggle"
    command "onWithTimedOff", [[name:"On time*", type:"NUMBER", description:"After how many seconds power will be turned Off [1..6500]"]]
    
    // Commands for capability.FirmwareUpdate
    command "updateFirmware"

    preferences {
        input(
            name: "logLevel",
            type: "enum",
            title: "Log verbosity",
            description: "<small>Select what type of messages appear in the \"Logs\" section.</small>",
            options: [
                "1" : "Debug - log everything",
                "2" : "Info - log important events",
                "3" : "Warning - log events that require attention",
                "4" : "Error - log errors"
            ],
            defaultValue: "1",
            required: true
        )
        
        // Inputs for capability.Switch
        input(
            name: "powerOnBehavior",
            type: "enum",
            title: "Power On behaviour",
            description: "<small>Select what happens after a power outage.</small>",
            options: [
                "TURN_POWER_ON": "Turn power On",
                "TURN_POWER_OFF": "Turn power Off",
                "RESTORE_PREVIOUS_STATE": "Restore previous state"
            ],
            defaultValue: "RESTORE_PREVIOUS_STATE",
            required: true
        )
    }
}

// ===================================================================================================================
// Implement default methods
// ===================================================================================================================

// Called when the device is first added
def installed() {
    Log.warn "Installing device ..."
    Log.warn "[IMPORTANT] For battery-powered devices, make sure that you keep your device as close as you can (less than 2inch / 5cm) to your Hubitat hub for at least 30 seconds. Otherwise the device will successfully pair but it won't work properly!"
}

// Called when the "Save Preferences" button is clicked
def updated(auto = false) {
    Log.info "Saving preferences${auto ? " (auto)" : ""} ..."
    List<String> cmds = []

    unschedule()

    if (logLevel == null) {
        logLevel = "1"
        device.updateSetting("logLevel", [value:logLevel, type:"enum"])
    }
    if (logLevel == "1") runIn 1800, "logsOff"
    Log.info "🛠️ logLevel = ${["1":"Debug", "2":"Info", "3":"Warning", "4":"Error"].get(logLevel)}"
    
    // Preferences for capability.Switch
    if (powerOnBehavior == null) {
        powerOnBehavior = "RESTORE_PREVIOUS_STATE"
        device.updateSetting("powerOnBehavior", [value:powerOnBehavior, type:"enum"])
    }
    Log.info "🛠️ powerOnBehavior = ${powerOnBehavior}"
    cmds += zigbee.writeAttribute(0x0006, 0x4003, 0x30, powerOnBehavior == "TURN_POWER_OFF" ? 0x00 : (powerOnBehavior == "TURN_POWER_ON" ? 0x01 : 0xFF))
    
    // Preferences for capability.HealthCheck
    schedule HEALTH_CHECK.schedule, "healthCheck"

    if (auto) return cmds
    Utils.sendZigbeeCommands cmds
}

// ===================================================================================================================
// Capabilities helpers
// ===================================================================================================================

// Handler method for scheduled job to disable debug logging
def logsOff() {
   Log.info '⏲️ Automatically reverting log level to "Info"'
   device.updateSetting("logLevel", [value:"2", type:"enum"])
}

// Helpers for capability.HealthCheck
def healthCheck() {
    Log.debug '⏲️ Automatically running health check'
    String healthStatus = state.lastRx == 0 || state.lastRx == null ? "unknown" : (now() - state.lastRx < Integer.parseInt(HEALTH_CHECK.thereshold) * 1000 ? "online" : "offline")
    Utils.sendEvent name:"healthStatus", value:healthStatus, type:"physical", descriptionText:"Health status is ${healthStatus}"
}

// ===================================================================================================================
// Implement Hubitat Capabilities
// ===================================================================================================================

// capability.Configuration
// Note: This method is also called when the device is initially installed
def configure(auto = false) {
    Log.warn "Configuring device${auto ? " (auto)" : ""} ..."
    if (!auto && device.currentValue("powerSource", true) == "battery") {
        Log.warn '[IMPORTANT] Click the "Configure" button immediately after pushing any button on the device in order to first wake it up!'
    }

    // Apply preferences first
    List<String> cmds = []
    cmds += updated(true)

    // Clear data (keep firmwareMT information though)
    device.getData()?.collect { it.key }.each { if (it != "firmwareMT") device.removeDataValue it }

    // Clear state
    state.clear()
    state.lastTx = 0
    state.lastRx = 0
    state.lastCx = DRIVER_VERSION

    // Configure IKEA Askvader On/Off Switch (E1836) specific Zigbee reporting
    // -- No reporting needed

    // Add IKEA Askvader On/Off Switch (E1836) specific Zigbee binds
    // -- No binds needed
    
    // Configuration for capability.Switch
    sendEvent name:"switch", value:"on", type:"digital", descriptionText:"Switch initialized to on"
    cmds += "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}" // On/Off cluster
    cmds += "he cr 0x${device.deviceNetworkId} 0x01 0x0006 0x0000 0x10 0x0000 0x0258 {01} {}" // Report OnOff (bool) at least every 10 minutes
    
    // Configuration for capability.HealthCheck
    sendEvent name:"healthStatus", value:"online", descriptionText:"Health status initialized to online"
    sendEvent name:"checkInterval", value:3600, unit:"second", descriptionText:"Health check interval is 3600 seconds"
    
    // Configuration for capability.PowerSource
    sendEvent name:"powerSource", value:"unknown", type:"digital", descriptionText:"Power source initialized to unknown"
    cmds += zigbee.readAttribute(0x0000, 0x0007) // PowerSource

    // Query Basic cluster attributes
    cmds += zigbee.readAttribute(0x0000, [0x0001, 0x0003, 0x0004, 0x0005, 0x000A, 0x4000]) // ApplicationVersion, HWVersion, ManufacturerName, ModelIdentifier, ProductCode, SWBuildID
    Utils.sendZigbeeCommands cmds

    Log.info "Configuration done; refreshing device current state in 7 seconds ..."
    runIn 7, "tryToRefresh"
}
private autoConfigure() {
    Log.warn "Detected that this device is not properly configured for this driver version (lastCx != ${DRIVER_VERSION})"
    configure true
}

// Implementation for capability.Switch
def on() {
    Log.debug "Sending On command"
    Utils.sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x01 0x0006 {114301}"])
}
def off() {
    Log.debug "Sending Off command"
    Utils.sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x01 0x0006 {114300}"])
}

def toggle() {
    Log.debug "Sending Toggle command"
    Utils.sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x01 0x0006 {114302}"])
}

def onWithTimedOff(onTime = 1) {
    Integer delay = onTime < 1 ? 1 : (onTime > 6500 ? 6500 : onTime)
    Log.debug "Sending OnWithTimedOff command"

    String payload = "00 ${zigbee.swapOctets(zigbee.convertToHexString(delay * 10, 4))} 0000"
    Utils.sendZigbeeCommands(["he raw 0x${device.deviceNetworkId} 0x01 0x01 0x0006 {114342 ${payload}}"])
}

// Implementation for capability.HealthCheck
def ping() {
    Log.warn "ping ..."
    Utils.sendZigbeeCommands(zigbee.readAttribute(0x0000, 0x0000))
    Log.debug "Ping command sent to the device; we'll wait 5 seconds for a reply ..."
    runIn 5, "pingExecute"
}

def pingExecute() {
    if (state.lastRx == 0) {
        return Log.info("Did not sent any messages since it was last configured")
    }

    Date now = new Date(Math.round(now() / 1000) * 1000)
    Date lastRx = new Date(Math.round(state.lastRx / 1000) * 1000)
    String lastRxAgo = TimeCategory.minus(now, lastRx).toString().replace(".000 seconds", " seconds")
    Log.info "Sent last message at ${lastRx.format("yyyy-MM-dd HH:mm:ss", location.timeZone)} (${lastRxAgo} ago)"

    Date thereshold = new Date(Math.round(state.lastRx / 1000 + Integer.parseInt(HEALTH_CHECK.thereshold)) * 1000)
    String theresholdAgo = TimeCategory.minus(thereshold, lastRx).toString().replace(".000 seconds", " seconds")
    Log.info "Will be marked as offline if no message is received for ${theresholdAgo} (hardcoded)"

    String offlineMarkAgo = TimeCategory.minus(thereshold, now).toString().replace(".000 seconds", " seconds")
    Log.info "Will be marked as offline if no message is received until ${thereshold.format("yyyy-MM-dd HH:mm:ss", location.timeZone)} (${offlineMarkAgo} from now)"
}

// Implementation for capability.Refresh
def refresh(buttonPress = true) {
    if (buttonPress) {
        Log.warn "Refreshing device current state ..."
        if (device.currentValue("powerSource", true) == "battery") {
            Log.warn '[IMPORTANT] Click the "Refresh" button immediately after pushing any button on the device in order to first wake it up!'
        }
    }

    List<String> cmds = []
    cmds += zigbee.readAttribute(0x0006, 0x0000, [:]) // OnOff
    cmds += zigbee.readAttribute(0x0006, 0x4003, [:]) // PowerOnBehavior
    Utils.sendZigbeeCommands cmds
}

// Implementation for capability.FirmwareUpdate
def updateFirmware() {
    Log.info "Looking for firmware updates ..."
    if (device.currentValue("powerSource", true) == "battery") {
        Log.warn '[IMPORTANT] Click the "Update Firmware" button immediately after pushing any button on the device in order to first wake it up!'
    }
    Utils.sendZigbeeCommands(zigbee.updateFirmware())
}

// ===================================================================================================================
// Handle incoming Zigbee messages
// ===================================================================================================================

def parse(String description) {
    Log.debug "description=[${description}]"

    // Auto-Configure device: configure() was not called for this driver version
    if (state.lastCx != DRIVER_VERSION) {
        state.lastCx = DRIVER_VERSION
        runInMillis(1500, "autoConfigure")
    }

    // Extract msg
    def msg = [:]
    if (description.startsWith("zone status")) msg += [ clusterInt:0x500, commandInt:0x00, isClusterSpecific:true ]
    if (description.startsWith("enroll request")) msg += [ clusterInt:0x500, commandInt:0x01, isClusterSpecific:true ]

    msg += zigbee.parseDescriptionAsMap description
    if (msg.containsKey("endpoint")) msg.endpointInt = Integer.parseInt(msg.endpoint, 16)
    if (msg.containsKey("sourceEndpoint")) msg.endpointInt = Integer.parseInt(msg.sourceEndpoint, 16)
    if (msg.containsKey("cluster")) msg.clusterInt = Integer.parseInt(msg.cluster, 16)
    if (msg.containsKey("command")) msg.commandInt = Integer.parseInt(msg.command, 16)
    Log.debug "msg=[${msg}]"

    state.lastRx = now()
    
    // Parse for capability.HealthCheck
    if (device.currentValue("healthStatus", true) != "online") {
        Utils.sendEvent name:"healthStatus", value:"online", type:"digital", descriptionText:"Health status changed to online"
    }

    // If we sent a Zigbee command in the last 3 seconds, we assume that this Zigbee event is a consequence of this driver doing something
    // Therefore, we mark this event as "digital"
    String type = state.containsKey("lastTx") && (now() - state.lastTx < 3000) ? "digital" : "physical"

    switch (msg) {

        // ---------------------------------------------------------------------------------------------------------------
        // Handle IKEA Askvader On/Off Switch (E1836) specific Zigbee messages
        // ---------------------------------------------------------------------------------------------------------------

        // ---------------------------------------------------------------------------------------------------------------
        // Handle capabilities Zigbee messages
        // ---------------------------------------------------------------------------------------------------------------
        
        // Events for capability.Switch
        
        // Report/Read Attributes: OnOff
        case { contains it, [clusterInt:0x0006, commandInt:0x0A, attrInt:0x0000] }:
        case { contains it, [clusterInt:0x0006, commandInt:0x01, attrInt:0x0000] }:
            String newState = msg.value == "00" ? "off" : "on"
            Utils.sendEvent name:"switch", value:newState, descriptionText:"Was turned ${newState}", type:type
        
            return Utils.processedZclMessage("${msg.commandInt == 0x0A ? "Report" : "Read"} Attributes Response", "OnOff=${newState}")
        
        // Read Attributes Response: powerOnBehavior
        case { contains it, [clusterInt:0x0006, commandInt:0x01, attrInt:0x4003] }:
            String newValue = ""
            switch (Integer.parseInt(msg.value, 16)) {
                case 0x00: newValue = "TURN_POWER_OFF"; break
                case 0x01: newValue = "TURN_POWER_ON"; break
                case 0xFF: newValue = "RESTORE_PREVIOUS_STATE"; break
                default: return Log.warn("Received attribute value: powerOnBehavior=${msg.value}")
            }
            powerOnBehavior = newValue
            device.updateSetting("powerOnBehavior",[ value:newValue, type:"enum" ])
        
            return Utils.processedZclMessage("Read Attributes Response", "PowerOnBehavior=${newValue}")
        
        // Other events that we expect but are not usefull for capability.Switch behavior
        case { contains it, [clusterInt:0x0006, commandInt:0x07] }:
            return Utils.processedZclMessage("Configure Reporting Response", "attribute=switch, data=${msg.data}")
        case { contains it, [clusterInt:0x0006, commandInt:0x04] }: // Write Attribute Response (0x04)
            return
        
        // Events for capability.HealthCheck
        case { contains it, [clusterInt:0x0000, attrInt:0x0000] }:
            return Log.warn("... pong")
        
        // Read Attributes Reponse: PowerSource
        case { contains it, [clusterInt:0x0000, commandInt:0x01, attrInt:0x0007] }:
            String powerSource = "unknown"
        
            // PowerSource := { 0x00:Unknown, 0x01:MainsSinglePhase, 0x02:MainsThreePhase, 0x03:Battery, 0x04:DC, 0x05:EmergencyMainsConstantlyPowered, 0x06:EmergencyMainsAndTransferSwitch }
            switch (msg.value) {
                case "01":
                case "02":
                case "05":
                case "06":
                    powerSource = "mains"
                    break
                case "03":
                    powerSource = "battery"
                    break
                case "04":
                    powerSource = "dc"
                    break
            }
            Utils.sendEvent name:"powerSource", value:powerSource, type:"digital", descriptionText:"Power source is ${powerSource}"
            return Utils.processedZclMessage("Read Attributes Response", "PowerSource=${msg.value}")

        // ---------------------------------------------------------------------------------------------------------------
        // Handle common messages (e.g.: received during pairing when we query the device for information)
        // ---------------------------------------------------------------------------------------------------------------

        // Device_annce: Welcome back! let's sync state.
        case { contains it, [endpointInt:0x00, clusterInt:0x0013, commandInt:0x00] }:
            Log.warn "Rejoined the Zigbee mesh; refreshing device state in 3 seconds ..."
            return runIn(3, "tryToRefresh")

        // Report/Read Attributes Response (Basic cluster)
        case { contains it, [clusterInt:0x0000, commandInt:0x01] }:
        case { contains it, [clusterInt:0x0000, commandInt:0x0A] }:
            Utils.zigbeeDataValue(msg.attrInt, msg.value)
            msg.additionalAttrs?.each { Utils.zigbeeDataValue(it.attrInt, it.value) }
            return Utils.processedZclMessage("${msg.commandInt == 0x0A ? "Report" : "Read"} Attributes Response", "cluster=0x${msg.cluster}, attribute=0x${msg.attrId}, value=${msg.value}")

        // Mgmt_Leave_rsp
        case { contains it, [endpointInt:0x00, clusterInt:0x8034, commandInt:0x00] }:
            return Log.warn("Device is leaving the Zigbee mesh. See you later, Aligator!")

        // Ignore the following Zigbee messages
        case { contains it, [commandInt:0x0A, isClusterSpecific:false] }:              // ZCL: Attribute report we don't care about (configured by other driver)
        case { contains it, [commandInt:0x0B, isClusterSpecific:false] }:              // ZCL: Default Response
        case { contains it, [clusterInt:0x0003, commandInt:0x01] }:                    // ZCL: Identify Query Command
        case { contains it, [endpointInt:0x00, clusterInt:0x8001, commandInt:0x00] }:  // ZDP: IEEE_addr_rsp
        case { contains it, [endpointInt:0x00, clusterInt:0x8004, commandInt:0x00] }:  // ZDP: Simple_Desc_rsp
        case { contains it, [endpointInt:0x00, clusterInt:0x8005, commandInt:0x00] }:  // ZDP: Active_EP_rsp
        case { contains it, [endpointInt:0x00, clusterInt:0x0006, commandInt:0x00] }:  // ZDP: MatchDescriptorRequest
        case { contains it, [endpointInt:0x00, clusterInt:0x8021, commandInt:0x00] }:  // ZDP: Mgmt_Bind_rsp
        case { contains it, [endpointInt:0x00, clusterInt:0x8038, commandInt:0x00] }:  // ZDP: Mgmt_NWK_Update_notify
            return

        // ---------------------------------------------------------------------------------------------------------------
        // Unexpected Zigbee message
        // ---------------------------------------------------------------------------------------------------------------
        default:
            Log.error "Sent unexpected Zigbee message: description=${description}, msg=${msg}"
    }
}

// ===================================================================================================================
// Logging helpers (something like this should be part of the SDK and not implemented by each driver)
// ===================================================================================================================

@Field Map Log = [
    debug: { if (logLevel == "1") log.debug "${device.displayName} ${it.uncapitalize()}" },
    info:  { if (logLevel <= "2") log.info  "${device.displayName} ${it.uncapitalize()}" },
    warn:  { if (logLevel <= "3") log.warn  "${device.displayName} ${it.uncapitalize()}" },
    error: { log.error "${device.displayName} ${it.uncapitalize()}" }
]

// ===================================================================================================================
// Helper methods (keep them simple, keep them dumb)
// ===================================================================================================================

@Field def Utils = [
    sendZigbeeCommands: { List<String> cmds ->
        if (cmds.isEmpty()) return
        List<String> send = delayBetween(cmds.findAll { !it.startsWith("delay") }, 1000)
        Log.debug "◀ Sending Zigbee messages: ${send}"
        state.lastTx = now()
        sendHubCommand new hubitat.device.HubMultiAction(send, hubitat.device.Protocol.ZIGBEE)
    },

    sendEvent: { Map event ->
        if (device.currentValue(event.name, true) != event.value || event.isStateChange) {
            Log.info "${event.descriptionText} [${event.type}]"
        } else {
            Log.debug "${event.descriptionText} [${event.type}]"
        }
        sendEvent event
    },

    dataValue: { String key, String value ->
        if (value == null || value == "") return
        Log.debug "Update data value: ${key}=${value}"
        updateDataValue key, value
    },

    zigbeeDataValue: { Integer attrInt, String value ->
        switch (attrInt) {
            case 0x0001: return Utils.dataValue("application", value)
            case 0x0003: return Utils.dataValue("hwVersion", value)
            case 0x0004: return Utils.dataValue("manufacturer", value)
            case 0x000A: return Utils.dataValue("type", "${!value ? "" : (value.split("") as List).collate(2).collect { "${Integer.parseInt(it.join(), 16) as char}" }.join()}")
            case 0x0005: return Utils.dataValue("model", value)
            case 0x4000: return Utils.dataValue("softwareBuild", value)
        }
    },

    processedZclMessage: { String type, String details ->
        Log.debug "▶ Processed ZCL message: type=${type}, status=SUCCESS, ${details}"
    },

    processedZdoMessage: { String type, String details ->
        Log.debug "▶ Processed ZDO message: type=${type}, status=SUCCESS, ${details}"
    }
]

// switch/case syntactic sugar
private boolean contains(Map msg, Map spec) {
    msg.keySet().containsAll(spec.keySet()) && spec.every { it.value == msg[it.key] }
}

// Call refresh() if available
private tryToRefresh() {
    try { refresh(false) } catch(ex) {}
}
