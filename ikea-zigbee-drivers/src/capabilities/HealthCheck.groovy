{{!--------------------------------------------------------------------------}}
{{# @definition }}
capability 'HealthCheck'
{{/ @definition }}
{{!--------------------------------------------------------------------------}}
{{# @fields }}

// Fields for capability.HealthCheck
import groovy.time.TimeCategory

@Field static final Map<String, String> HEALTH_CHECK = [
    'schedule': '{{ params.schedule }}', // Health will be checked using this cron schedule
    'thereshold': '{{ params.thereshold }}' // When checking, mark the device as offline if no Zigbee message was received in the last {{ params.thereshold }} seconds
]
{{/ @fields }}
{{!--------------------------------------------------------------------------}}
{{# @attributes }}

// Attributes for capability.HealthCheck
attribute 'healthStatus', 'enum', ['offline', 'online', 'unknown']
{{/ @attributes }}
{{!--------------------------------------------------------------------------}}
{{# @updated }}

// Preferences for capability.HealthCheck
schedule HEALTH_CHECK.schedule, 'healthCheck'
{{/ @updated }}
{{!--------------------------------------------------------------------------}}
{{# @helpers }}

// Helpers for capability.HealthCheck
void healthCheck() {
    log_debug '⏲️ Automatically running health check'
    String healthStatus = state.lastRx == 0 || state.lastRx == null ? 'unknown' : (now() - state.lastRx < Integer.parseInt(HEALTH_CHECK.thereshold) * 1000 ? 'online' : 'offline')
    utils_sendEvent name:'healthStatus', value:healthStatus, type:'physical', descriptionText:"Health status is ${healthStatus}"
}
{{/ @helpers }}
{{!--------------------------------------------------------------------------}}
{{# @configure }}

// Configuration for capability.HealthCheck
sendEvent name:'healthStatus', value:'online', descriptionText:'Health status initialized to online'
sendEvent name:'checkInterval', value:{{ params.checkInterval }}, unit:'second', descriptionText:'Health check interval is {{ params.checkInterval }} seconds'
{{/ @configure }}
{{!--------------------------------------------------------------------------}}
{{# @implementation }}

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
{{/ @implementation }}
{{!--------------------------------------------------------------------------}}
{{# @parse }}

// Parse for capability.HealthCheck
if (device.currentValue('healthStatus', true) != 'online') {
    utils_sendEvent name:'healthStatus', value:'online', type:'digital', descriptionText:'Health status changed to online'
}
{{/ @parse }}
{{!--------------------------------------------------------------------------}}
{{# @events }}

// Events for capability.HealthCheck
// ===================================================================================================================

case { contains it, [clusterInt:0x0000, attrInt:0x0000] }:
    log_warn '... pong'
    return
{{/ @events }}
{{!--------------------------------------------------------------------------}}
