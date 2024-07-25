f/*
 * Watchtower app for Hubitat
 *
 * View dashboards for your Hubitat hub metrics.
 *
 * @see https://github.com/dan-danache/hubitat
 */
import groovy.transform.Field

import java.nio.file.NoSuchFileException
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.ZoneId
import java.util.Collections
import java.math.RoundingMode

import groovy.json.JsonBuilder

import com.hubitat.app.DeviceWrapper

@Field static final String APP_NAME = 'Watchtower'
@Field static final String APP_VERSION = '1.0.0'
@Field static final def URL_PATTERN = ~/^https?:\/\/[^\/]+(.+)/
@Field static final String APP_ICON = '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'

@Field static final Map SUPPORTED_ATTRIBUTES = [
    acceleration: [ unit:'% active', probe:{ device -> "${device.currentValue('acceleration')}" == 'active' ? 100 : 0 } ],
    airQualityIndex: [ unit:'/500', probe:{ device -> "${device.currentValue('airQualityIndex')}" } ],
    amperage: [ unit:'A', probe:{ device -> "${device.currentValue('amperage')}" } ],
    battery: [ unit:'% full', probe:{ device -> "${device.currentValue('battery')}" } ],
    camera: [ unit:'% on', probe:{ device -> "${device.currentValue('camera')}" == 'on' ? 100 : 0 } ],
    carbonDioxide: [ unit:'ppm', probe:{ device -> "${device.currentValue('carbonDioxide')}" } ],
    contact: [ unit:'% open', probe:{ device -> "${device.currentValue('contact')}" == 'open' ? 100 : 0 } ],
    coolingSetpoint: [ unit:'°', probe:{ device -> "${device.currentValue('coolingSetpoint')}" } ],
    door: [ unit:'% open', probe:{ device -> "${device.currentValue('door')}" == 'open' ? 100 : 0 } ],
    energy: [ unit:'kWh', probe:{ device -> "${device.currentValue('energy')}" } ],
    filterStatus: [ unit:'% normal', probe:{ device -> "${device.currentValue('filterStatus')}" == 'normal' ? 100 : 0 } ],
    frequency: [ unit:'Hz', probe:{ device -> "${device.currentValue('frequency')}" } ],
    goal: [ unit:'steps', probe:{ device -> "${device.currentValue('goal')}" } ],
    heatingSetpoint: [ unit:'°', probe:{ device -> "${device.currentValue('heatingSetpoint')}" } ],
    humidity: [ unit:'%', probe:{ device -> "${device.currentValue('humidity')}" } ],
    illuminance: [ unit:'lx', probe:{ device -> "${device.currentValue('illuminance')}" } ],
    lock: [ unit:'% locked', probe:{ device -> "${device.currentValue('lock')}" == 'locked' ? 100 : 0 } ],
    lqi: [ unit:'lqi', probe:{ device -> "${device.currentValue('lqi')}" } ],
    motion: [ unit:'% active', probe:{ device -> "${device.currentValue('motion')}" == 'active' ? 100 : 0 } ],
    naturalGas: [ unit:'% detected', probe:{ device -> "${device.currentValue('naturalGas')}" == 'detected' ? 100 : 0 } ],
    networkStatus: [ unit:'% online', probe:{ device -> "${device.currentValue('networkStatus')}" == 'online' ? 100 : 0 } ],
    pH: [ unit:'pH', probe:{ device -> "${device.currentValue('pH')}" } ],
    power: [ unit:'W', probe:{ device -> "${device.currentValue('power')}" } ],
    presence: [ unit:'% present', probe:{ device -> "${device.currentValue('presence')}" == 'present' ? 100 : 0 } ],
    pressure: [ unit:'psi', probe:{ device -> "${device.currentValue('pressure')}" } ],
    rate: [ unit:'LPM', probe:{ device -> "${device.currentValue('rate')}" } ],
    rssi: [ unit:'rssi', probe:{ device -> "${device.currentValue('rssi')}" } ],
    securityKeypad: [ unit:'% armed', probe:{ device -> "${device.currentValue('securityKeypad')}".startsWith('armed') ? 100 : 0 } ],
    sessionStatus: [ unit:'% running', probe:{ device -> "${device.currentValue('sessionStatus')}" == 'running' ? 100 : 0 } ],
    shock: [ unit:'% detected', probe:{ device -> "${device.currentValue('shock')}" == 'detected' ? 100 : 0 } ],
    sleeping: [ unit:'% sleeping', probe:{ device -> "${device.currentValue('sleeping')}" == 'sleeping' ? 100 : 0 } ],
    smoke: [ unit:'% detected', probe:{ device -> "${device.currentValue('smoke')}" == 'detected' ? 100 : 0 } ],
    sound: [ unit:'% detected', probe:{ device -> "${device.currentValue('sound')}" == 'detected' ? 100 : 0 } ],
    soundPressureLevel: [ unit:'dB', probe:{ device -> "${device.currentValue('soundPressureLevel')}" } ],
    steps: [ unit:'steps', probe:{ device -> "${device.currentValue('steps')}" } ],
    'switch': [ unit:'% on', probe:{ device -> "${device.currentValue('switch')}" == 'on' ? 100 : 0 } ],
    tamper: [ unit:'% detected', probe:{ device -> "${device.currentValue('tamper')}" == 'detected' ? 100 : 0 } ],
    temperature: [ unit:'°', probe:{ device -> "${device.currentValue('temperature')}" } ],
    transportStatus: [ unit:'% playing', probe:{ device -> "${device.currentValue('transportStatus')}" == 'playing' ? 100 : 0 } ],
    valve: [ unit:'% open', probe:{ device -> "${device.currentValue('valve')}" == 'open' ? 100 : 0 } ],
    water: [ unit:'% wet', probe:{ device -> "${device.currentValue('water')}" == 'wet' ? 100 : 0 } ],
    windowBlind: [ unit:'% open', probe:{ device -> "${device.currentValue('windowBlind')}".contains('open') ? 100 : 0 } ],
    windowShade: [ unit:'% open', probe:{ device -> "${device.currentValue('windowShade')}".contains('open') ? 100 : 0 } ],
    voltage: [ unit:'V', probe:{ device -> "${device.currentValue('voltage')}" } ],

    // Non-standard attributes
    pm25: [ unit:'μg/m3', probe:{ device -> "${device.currentValue('pm25')}" } ],
    vocIndex: [ unit:'', probe:{ device -> "${device.currentValue('vocIndex')}" } ],
]

definition(
    name: APP_NAME,
    namespace: 'dandanache',
    author: 'Dan Danache',
    description: 'Build dashboards for your smart devices',
    documentationLink: 'https://community.hubitat.com/t/release-watchtower-app/134375',
    importUrl: 'https://raw.githubusercontent.com/dan-danache/hubitat/watchtower_1.0.0/watchtower-app/watchtower.groovy',
    category: 'Utility',
    singleInstance: true,
    installOnOpen: true,
    iconUrl: '',
    iconX2Url: '',
    oauth: true,
)

// ===================================================================================================================
// Standard app methods
// ===================================================================================================================

def installed() {
    log.info "${app.getLabel()} has been installed"
    unschedule()
    schedule '0 0/5 * ? * * *', 'collectMetrics'
}
def updated() {
    log.info "${app.getLabel()} has been updated"
}
def refresh() {
    log.info "${app.getLabel()} has been refreshed"
}
private void debug(message) {
    if (logEnable) log.debug "${APP_NAME} ▸ ${message}"
}
private void info(message) {
    log.info "${APP_NAME} ▸ ${message}"
}
private void warn(message) {
    log.warn "${APP_NAME} ▸ ${message}"
}

// ===================================================================================================================
// Button handler
// ===================================================================================================================

def appButtonHandler(String buttonName) {
    List<String> dashboardList = app.getSetting('dashboards') ?: []

    if (buttonName == 'addDashboard') {
        log.info "addDashboard clicked"

        // Find next empty position for insertion
        state.position = (dashboardList ?: []).size

        // Clear form
        app.removeSetting 'dashboardName'

        // Update action
        state.action = 'add'
    }

    if (buttonName == 'saveDashboard') {
        log.info "saveDashboard ${dashboardName}"
        String newDashboardName = "${dashboardName}".trim()
        if (!dashboardList.contains(newDashboardName)) {
            dashboardList[state.position] = newDashboardName
            app.updateSetting('dashboards', dashboardList)
        }

        // Update action
        state.remove 'position'
        state.action = 'list'
    }

    if (buttonName.startsWith('removeDashboard_')) {
        state.position = Integer.parseInt(buttonName.substring(16))

        // Update action
        state.action = 'confirm'
    }

    if (buttonName == 'removeDashboard') {

        // Cleanup settings
        String removedDashboardName = dashboardList.remove(state.position)
        app.updateSetting('dashboards', dashboardList)
        app.removeSetting "b.${removedDashboardName}"

        // Update action
        state.remove 'position'
        state.action = 'list'
    }

    if (buttonName.startsWith('editDashboard_')) {
        state.position = Integer.parseInt(buttonName.substring(14))
        state.action = 'view'
        app.updateSetting('dashboardName', dashboardList[state.position])
    }

    if (buttonName == 'addDevice') {

        // Find next empty position for insertion
        int nextPosition = 1
        while (app.getSetting("d.${nextPosition}") != null && app.getSetting("a.${nextPosition}") != null) nextPosition++
        state.position = nextPosition

        // Clear form
        app.removeSetting "d.${nextPosition}"
        app.removeSetting "a.${nextPosition}"

        // Update action
        state.action = 'add'
    }

    if (buttonName.startsWith('viewDevice_')) {
        state.position = Integer.parseInt(buttonName.substring(11))
        state.highlightPosition = state.position
        state.action = 'view'
    }

    if (buttonName == 'removeDevice') {

        // Remove CSV files
        deleteDataFiles(app.getSetting("d.${state.position}"))

        // Cleanup settings
        app.removeSetting "d.${state.position}"
        app.removeSetting "a.${state.position}"

        // Reset lastPosition; to be calculated on next list render
        state.remove 'lastPosition'

        // Update action
        state.remove 'position'
        state.action = 'list'
    }

    if (buttonName == 'cancel') {

        // Clear form
        if (state.action == 'add') {
            app.removeSetting "d.${state.position}"
            app.removeSetting "a.${state.position}"
        }

        // Update action
        state.remove 'position'
        state.action = 'list'
    }

    if (buttonName == 'saveDevice' || buttonName == 'close') {

        // Update lastPosition && highlightPosition
        state.lastPosition = Math.max(state.position, state.lastPosition)
        state.highlightPosition = state.position

        // Update action
        state.remove 'position'
        state.action = 'list'
    }
}

// ===================================================================================================================
// Implement Pages
// ===================================================================================================================

preferences {
    page name: 'main'
    page name: 'devices'
    page name: 'dashboards'
    page name: 'settings'
    page name: 'changelog'
}

Map main() {
    def showInstall = app.getInstallationState() == 'INCOMPLETE'
    dynamicPage(name:'main', install:true, uninstall:!showInstall) {

        if (app.getInstallationState() != 'COMPLETE') {
            section {
                paragraph 'Click the "Done" button to complete the app installation.'
            }
        } else {
            if (!state.accessToken) createAccessToken()

            // Cleanup devices page state
            state.remove 'position'
            state.action = 'list'

            section {
                href(name:'devicesLink', title:'Devices', description:'Select devices to monitor', page:'devices', required:false)
                href(name:'dashboardsLink', title:'Dashboards', description:'Manage dashboards', page:'dashboards', required:false)
                href(name:'settingsLink', title:'Settings', description:'Configure metrics storage limits', page:'settings', required:false)
                href(name:'changelogLink', title:'Change log', description:'See latest application changes', page:'changelog', required:false)

                // Preferences
                input(name:'useDarkTheme', type:'bool', title:'Use dark theme', defaultValue:false, submitOnChange:true)
                input(name:'logEnable', type:'bool', title:'Enable debug logging', defaultValue:false, submitOnChange:true)
            }
        }
    }
}

Map devices() {
    dynamicPage(name:'devices', title:'Devices', install:false, uninstall:false) {

        // Highlight and clear last added/viewed position
        Integer highlightPosition = null
        if (state.action != 'add') {
            highlightPosition = state.highlightPosition
            state.remove 'highlightPosition'
        }

        // Render table
        List devices = collectDeviceConfiguration()
        String table = renderInfoBox('Click the button below to configure your first device')
        if (devices.size != 0) {
            table = '<div style="overflow-x:auto; border: 1px rgba(0,0,0,.12) solid"><table id="app-table" class="mdl-data-table tstat-col"><tbody>'
            int pos = 1
            devices.each {
                table += """
                    <tr${highlightPosition == it[0] ? ' id="highlighted-row"' : ''}>
                        <td>${renderButton("viewDevice_${it[0]}", "${it[1]}<div class=\"text-600\">${it[2].join(', ')}</div>", 'View device configuration', 'view-btn')}</td>
                    </tr>
                """
            }
            table += '</tbody></table></div>'
        }

        section {
            paragraph """\
                ${renderCommonStyle()}${table}
                <script type="text/javascript">
                    if (document.getElementById('highlighted-row')) document.getElementById('highlighted-row').scrollIntoView({
                        behavior: 'smooth',
                        block: 'center'
                    })
                </script>
            """
        }

        // Render add button
        section {
            input(name:'addDevice', title:'➕ Add device&nbsp;&nbsp;&nbsp;', type:'button')
        }

        // Render add/view page
        if (state.position != null) {
            def device = app.getSetting("d.${state.position}")
            List<String> attributes = app.getSetting("a.${state.position}")

            // Render add page
            if (state.action == 'add') {
                section {
                    input(
                        name: "d.${state.position}",
                        title: 'Select device',
                        type: 'capability.*',
                        multiple: false,
                        showFilter: true,
                        required: true,
                        submitOnChange: true
                    )

                    if (device != null) {
                        List<String> allAttr = device.supportedAttributes
                            .collect { it.name }
                            .findAll { SUPPORTED_ATTRIBUTES.containsKey(it) }
                            .unique()
                            .sort()

                        // Check for device with unsupported attributes
                        if (allAttr.size == 0) {
                            disableSaveButton = true
                            paragraph renderInfoBox("<b>${device.displayName}</b> contains no supported attributes. Please select another device!")

                        // Check for duplicate device
                        } else if (devices.any { device.id == it[1].id } ) {
                            disableSaveButton = true
                            paragraph renderInfoBox("<b>${device.displayName}</b> is already configured. Please select another device!")
                        } else {

                            // Device changed; clear attributes
                            if (attributes != null && !allAttr.containsAll(attributes)) {
                                app.removeSetting "a.${state.position}"
                            }

                            input(
                                name: "a.${state.position}",
                                title: 'Select attributes',
                                type: 'enum',
                                options: allAttr,
                                multiple: true,
                                required: true,
                                submitOnChange: true
                            )
                        }
                    }
                }
            }

            // Render view page
            if (state.action == 'view') {
                section {
                    paragraph """
                        <b>${device}</b>
                        <div class="text-600">${attributes.join(', ')}</div>
                        <hr>
                        Data files
                        <ul>
                            <li><a href="/local/wt_${device.id}_5m.csv" target="_blank">wt_${device.id}_5m.csv <i class="pi pi-external-link"></i></a></li>
                            <li><a href="/local/wt_${device.id}_1h.csv" target="_blank">wt_${device.id}_1h.csv <i class="pi pi-external-link"></i></a></li>
                            <li><a href="/local/wt_${device.id}_1d.csv" target="_blank">wt_${device.id}_1d.csv <i class="pi pi-external-link"></i></a></li>
                            <li><a href="/local/wt_${device.id}_1w.csv" target="_blank">wt_${device.id}_1w.csv <i class="pi pi-external-link"></i></a></li>
                        </ul>
                        ${renderInfoBox('Removing this device configuration will also remove the data files')}
                    """
                }
            }

            section {
                boolean disableSaveButton = app.getSetting("d.${state.position}") == null || app.getSetting("a.${state.position}") == null
                paragraph """\
                    <div class="p-dialog-mask" style="display:${state.action == 'add' || state.action == 'view' ? 'flex' : 'none'}; position: fixed; height: 100%; width: 100%; left: 0px; top: 0px; justify-content: center; align-items: center; pointer-events: none; z-index: 3203;" data-pc-section="mask">
                        <div class="p-dialog p-component" style="min-width: 30vw; display: flex; flex-direction: column; pointer-events: auto;" role="dialog" data-pc-name="dialog" data-pc-section="root" data-pd-focustrap="true">
                            <div class="p-dialog-header" data-pc-section="header">
                                <span class="p-dialog-title" data-pc-section="title">${state.action == 'add' ? 'Add' : 'View'} device configuration</span>
                            </div>
                            <div id="dialog-body" class="p-dialog-content" data-pc-section="content"></div>
                            <div id="dialog-footer" class="p-dialog-footer" data-pc-section="footer">
                                ${ state.action != 'view' ? '' : """
                                    ${renderButton('removeDevice', '🗑️&nbsp;&nbsp;Remove', 'Remove device configuration', 'mdl-button mdl-js-button mdl-button--accent mdl-button--raised', 'dialog-btn btn-remove')}
                                    ${renderButton('close', '✖&nbsp;&nbsp;Close', 'Close view', 'mdl-button mdl-js-button mdl-button--raised', 'dialog-btn btn-close')}
                                """}
                                ${ state.action != 'add' ? '' : """
                                    ${renderButton('cancel', '✖&nbsp;&nbsp;Cancel', 'Cancel add action', 'mdl-button mdl-js-button mdl-button--raised', 'dialog-btn')}
                                    ${renderButton(disableSaveButton, 'saveDevice', '✔&nbsp;&nbsp;Save', 'Save device configuration', 'mdl-button mdl-button--primary mdl-js-button mdl-button--raised', 'dialog-btn')}
                                """}
                            </div>
                        </div>
                    </div>
                    <script type="text/javascript">
                        document.getElementById('dialog-body').prepend(document.querySelectorAll('div[style="margin-bottom:15px;"]')[2])
                    </script>
                """
            }
        }
    }
}

Map dashboards() {
    //app.removeSetting 'dashboards'
    List<String> dashboardList = app.getSetting('dashboards') ?: []
    dynamicPage(name:'dashboards', title:'Dashboards', install:false, uninstall:false) {
        String table = renderInfoBox('Click the button below to add your first dashboard')
        int idx = 0
        if (dashboardList.size != 0) {
            table = '<div style="overflow-x:auto; border: 1px rgba(0,0,0,.12) solid"><table id="app-table" class="mdl-data-table tstat-col"><tbody>'
            dashboardList.each {
                table += """
                    <tr>
                        <td><a href="${buildDashboardURL(it)}" target="_blank">${it} <i class="pi pi-external-link"></i></a></td>
                        <td class="tbl-icon">${renderButton("editDashboard_${idx}", '✏️', 'Rename dashboard', 'view-btn')}</td>
                        <td class="tbl-icon">${renderButton("removeDashboard_${idx}", '🗑️', 'Remove dashboard', 'view-btn')}</td>
                    </tr>
                """
                idx++
            }
            table += '</tbody></table></div>'
        }

        section {
            paragraph "${renderCommonStyle()}${table}"
        }

        // Render add button
        section {
            input(name:'addDashboard', title:'➕ Add dashboard&nbsp;&nbsp;&nbsp;', type:'button')
        }

        // Render add/view page
        if (state.action == 'add' || state.action == 'view') {
            section {
                input(
                    name: 'dashboardName',
                    title: 'Dashboard name',
                    type: 'text',
                    required: true,
                    submitOnChange: true
                )
            }

            section {
                boolean disableSaveButton = app.getSetting('dashboardName') == null || app.getSetting('dashboardName').trim() == '' || app.getSetting('dashboardName') == dashboardList[state.position]
                paragraph """\
                    <div class="p-dialog-mask" style="display:${state.action == 'add' || state.action == 'view' ? 'flex' : 'none'}; position: fixed; height: 100%; width: 100%; left: 0px; top: 0px; justify-content: center; align-items: center; pointer-events: none; z-index: 3203;" data-pc-section="mask">
                        <div class="p-dialog p-component" style="display: flex; flex-direction: column; pointer-events: auto;" role="dialog" data-pc-name="dialog" data-pc-section="root" data-pd-focustrap="true">
                            <div class="p-dialog-header" data-pc-section="header">
                                <span class="p-dialog-title" data-pc-section="title">${state.action == 'add' ? 'Add' : 'Rename'} dashboard</span>
                            </div>
                            <div id="dialog-body" class="p-dialog-content" data-pc-section="content"></div>
                            <div id="dialog-footer" class="p-dialog-footer" data-pc-section="footer">
                                ${renderButton('cancel', '✖&nbsp;&nbsp;Cancel', 'Cancel add action', 'mdl-button mdl-js-button mdl-button--raised', 'dialog-btn')}
                                ${renderButton(disableSaveButton, 'saveDashboard', '✔&nbsp;&nbsp;Save', 'Save dashboard', 'mdl-button mdl-button--primary mdl-js-button mdl-button--raised', 'dialog-btn')}
                            </div>
                        </div>
                    </div>
                    <script type="text/javascript">
                        document.getElementById('dialog-body').prepend(document.querySelectorAll('div[style="margin-bottom:15px;"]')[2])
                    </script>
                """
            }
        }

        // Render confirm delete page
        if (state.action == 'confirm') {
            section {
                paragraph """\
                    <div class="p-dialog-mask" style="display:flex; position: fixed; height: 100%; width: 100%; left: 0px; top: 0px; justify-content: center; align-items: center; pointer-events: none; z-index: 3203;" data-pc-section="mask">
                        <div class="p-dialog p-component" style="display: flex; flex-direction: column; pointer-events: auto;" role="dialog" data-pc-name="dialog" data-pc-section="root" data-pd-focustrap="true">
                            <div class="p-dialog-header" data-pc-section="header">
                                <span class="p-dialog-title" data-pc-section="title">Confirm</span>
                            </div>
                            <div id="dialog-body" class="p-dialog-content" data-pc-section="content">
                                Remove the <b>${dashboardList[state.position]}</b> dashboard now?<br><br>
                            </div>
                            <div id="dialog-footer" class="p-dialog-footer" data-pc-section="footer">
                                ${renderButton('cancel', 'No', 'Cancel remove action', 'mdl-button mdl-js-button mdl-button--raised', 'dialog-btn')}
                                ${renderButton('removeDashboard', 'Yes', 'Remove dashboard', 'mdl-button mdl-js-button mdl-button--accent mdl-button--raised', 'dialog-btn')}
                            </div>
                        </div>
                    </div>
                """
            }
        }
    }
}

Map settings() {
    dynamicPage(name:'settings', title:'Settings', install:false, uninstall:false) {
        section {
            input(
                name: 'conf_5MinMaxLines',
                title: 'Max records with 5 min accuracy<br><span class="text-600" style="font-size:.85em">default 864 (3 days), min 288 (1 day)</span>',
                type: 'number',
                required: true,
                defaultValue: 864,
                range: '288..10000',
                width: 6,
            )
            input(
                name: 'conf_1HourMaxLines',
                title: 'Max records with 1 hour accuracy<br><span class="text-600" style="font-size:.85em">default 744 (1 month), min 168 (1 week)</span>',
                type: 'number',
                required: true,
                defaultValue: 744,
                range: '168..10000',
                width: 6,
            )
            input(
                name: 'conf_1DayMaxLines',
                title: 'Max records with 1 day accuracy<br><span class="text-600" style="font-size:.85em">default 732 (2 years), min 366 (1 year)</span>',
                type: 'number',
                required: true,
                defaultValue: 732,
                range: '366..10000',
                width: 6,
            )
            input(
                name: 'conf_1WeekMaxLines',
                title: 'Max records with 1 week accuracy<br><span class="text-600" style="font-size:.85em">default 522 (10 years), min 105 (2 years)</span>',
                type: 'number',
                required: true,
                defaultValue: 522,
                range: '105..2600',
                width: 6,
            )
        }
    }
}

Map changelog() {
    dynamicPage(name:'changelog', title:'Change log', install:false, uninstall:false) {
        section('v1.0.0 - 2024-02-26', hideable:true, hidden:false) {
            paragraph '''\
                <ul>
                    <li>Initial release</li>
                </ul>
                <style>
                    .mdl-cell > div { white-space:normal !important }
                    ul { margin:0; padding-left:.5em }
                </style>
            '''
        }
    }
}

// ===================================================================================================================
// Implement Mappings
// ===================================================================================================================

mappings {
    path('/watchtower.html') { action:[ GET:'getDashboardHtmlMapping' ] }
    path('/watchtower.js') { action:[ GET:'getDashboardJsMapping' ] }
    path('/icon.png') { action:[ GET:'getIconMapping' ] }
    path('/app.webmanifest') { action:[ GET:'getAppManifestMapping' ] }
    path('/grid-layout.json') { action:[ GET:'getGridLayoutMapping', PUT:'setGridLayoutMapping' ] }
    path('/monitored-devices.json') { action:[ GET:'getMonitoredDevicesMapping' ] }
    path('/supported-attributes.json') { action:[ GET:'getSupportedAttributesMapping' ] }
}

def getDashboardHtmlMapping() {
    debug "Proxying watchtower.html to ${request.HOST} (${request.requestSource})"
    if (params.name == null) throw new RuntimeException('Missing "name" query param')
    return render(
        status: 200,
        contentType: 'text/html',
        data: new String(downloadHubFile('watchtower.html'), 'UTF-8')
            .replaceAll('\\$\\{access_token\\}', "${state.accessToken}")
            .replaceAll('\\$\\{dashboard_name\\}', "${params.name}")
    )
}

def getDashboardJsMapping() {
    debug "Proxying watchtower.js to ${request.HOST} (${request.requestSource})"
    return render(
        status: 200,
        contentType: 'text/javascript',
        data: new String(downloadHubFile('watchtower.js'), 'UTF-8')
    )
}

def getIconMapping() {
    debug 'Returning app icon'
    return render(
        status: 200,
        contentType: 'image/png',
        data: APP_ICON.decodeBase64()
    )
}

def getAppManifestMapping() {
    debug 'Returning PWA manifest'
    if (params.name == null) throw new RuntimeException('Missing "name" query param')
    return render(
        status: 200,
        contentType: 'application/manifest+json',
        data: """\
        {
            "id": "${java.util.UUID.nameUUIDFromBytes(params.name.getBytes())}",
            "name": "${params.name}",
            "short_name": "${params.name}",
            "description": "View metrics for your smart devices.",
            "start_url": "${buildDashboardURL(params.name)}",
            "icons": [{
                "src": "data:image/png;base64,${APP_ICON}",
                "sizes": "512x512",
                "type": "image/png",
                "purpose": "maskable"
            }],
            "categories": ["utilities"],
            "display": "standalone",
            "orientation": "portrait",
            "theme_color": "${useDarkTheme ? "#073642" : "#eee8d5"}",
            "background_color": "${useDarkTheme ? "#073642" : "#eee8d5"}"
        }
        """
    )
}

def getGridLayoutMapping() {
    debug "Returning grid layout for dashboard: ${params.name}"
    if (params.name == null) throw new RuntimeException('Missing "name" query param')

    List<String> dashboardList = app.getSetting('dashboards') ?: []
    int idx = dashboardList.findIndexOf { it == "${params.name}" }
    if (idx == -1) return render(status:200, contentType:'application/json', data:'{"status": false}')
    return render(status:200, contentType:'application/json', data:state["g.${idx}"] ?: '{"panels":[]}')
}

def setGridLayoutMapping() {
    debug "Saving grid layout for dashboard: ${params.name}"
    if (params.name == null) throw new RuntimeException('Missing "name" query param')

    List<String> dashboardList = app.getSetting('dashboards') ?: []
    int idx = dashboardList.findIndexOf { it == params.name }
    if (idx == -1) return render(status:200, contentType:'application/json', data:'{"status": false}')

    runIn(1, 'saveGridLayout', [data: [idx:idx, json:"${request.body}"]])
    return render(status:200, contentType:'application/json', data:'{"status": true}')
}

def getMonitoredDevicesMapping() {
    debug "Returning monitored devices list"
    List devices = collectDeviceConfiguration().collect { return [id:it[1].id, name:it[1].displayName, attrs:it[2]] }
    return render(status:200, contentType:'application/json', data:new JsonBuilder(devices).toString())
}

def getSupportedAttributesMapping() {
    debug "Returning supported attributes list"
    Map attributes = SUPPORTED_ATTRIBUTES.collectEntries{key, val -> [key, [unit: val.unit]]}
    attributes.temperature.unit += location.temperatureScale
    return render(status:200, contentType:'application/json', data:new JsonBuilder(attributes).toString())
}

def saveGridLayout(data) {
    state["g.${data.idx}"] = data.json
}

// ===================================================================================================================
// Helper functions
// ===================================================================================================================

String renderCommonStyle() {
    return '''
    <style>
        .mdl-cell > div { white-space:normal !important }
        .mdl-grid { padding: 0 !important }

        #app-table {
            background-color: inherit;
            border: 1px rgba(0,0,0,.12) solid;
            font-size: 15px;
            border-collapse: collapse;
            width: 100%;
        }
        #app-table tr:hover { background-color: transparent }
        #app-table th {
            font-weight: bold;
            border-bottom: 2px rgba(0,0,0,.12) solid;
        }
        #app-table td {
            text-align: left;
            padding: 0;
            white-space: nowrap;
            text-overflow: ellipsis;
        }
        #app-table td.tbl-icon { width: 1em }
        .view-btn {
            border: 0;
            padding: .5em;
            margin: 0;
            background-color: transparent;
            color: #1a77c9;
            cursor: pointer;
            font-size: 1rem;
            text-align: left;
            width: 100%;
        }
        .view-btn:hover { background-color: var(--gray-300) }
        .view-btn > div { margin-top: .2em; cursor: default }
        #highlighted-row td { animation: background-fade 10s forwards }
        #app-table a { margin-left:.5em }
        @keyframes background-fade { 0% { background-color: #FFDAA3 }}

        #dialog-body { padding-bottom: 0 !important }
        #dialog-body > div { margin-bottom: 0 !important }
        #dialog-footer { display: block; text-align: right }
        #dialog-footer > div.form-group { display: none }
        #dialog-footer > div.dialog-btn { display: inline-block; margin-left: 1em }
        #dialog-footer > div.btn-remove { margin: 0; float: left }
        #dialog-footer button { position: static }
        #dialog-body hr { margin: .5em 0 }
        #dialog-body i { font-size: .85em }

        .form-warning {
            padding: .8em 1em;
            color: #856404;
            background-color: #fff3cd;
            border: 1px #ffeeba solid;
            border-radius: .3em;
            margin-left: -8px;
            margin-right: -8px;
            font-size: .85em;
        }
    </style>
    '''
}

String renderButton(String name, String label, String tooltip=null, String buttonClass=null, String containerClass=null) {
    return renderButton(false, name, label, tooltip, buttonClass, containerClass)
}

String renderButton(Boolean disabled, String name, String label, String tooltip=null, String buttonClass=null, String containerClass=null) {
    return """
    <div class="form-group">
        <input type="hidden" name="${name}.type" value="button">
        <input type="hidden" name="${name}.multiple" value="false">
    </div>
    <div${containerClass != null ? " class=\"${containerClass}\"": ''}>
        <button type="button" id="settings[${name}]" class="submitOnChange${buttonClass != null ? " ${buttonClass}": ''}" value="button"${tooltip != null ? "title=\"${tooltip}\"" : ''}
        ${ disabled ? 'disabled="true"' : '' }
        >${label}</button>
        <input type="hidden" name="settings[${name}]" value="">
    </div>
    """
}

String renderInfoBox(String message) {
    return "<div class=\"form-warning\">${message}</div>"
}

def collectDeviceConfiguration() {
    List retVal = []
    Integer lastPosition = state.lastPosition ?: 500
    for (int position = 1; position <= lastPosition; position++) {

        // Skip current entry that is in the add form right now
        if (state.action == 'add' && state.position == position) continue

        // Skip uncomplete/broken entries
        if (app.getSetting("d.${position}") == null || app.getSetting("a.${position}") == null) continue
        retVal.add([ position, app.getSetting("d.${position}"), app.getSetting("a.${position}") ])
    }
    state.lastPosition = retVal.size == 0 ? 0 : retVal.last()[0]
    return retVal.sort { it[1].label }
}

def buildURL(String fileName) {
    String prefix = useCloudLinks == true
        ? "${getApiServerUrl()}/${hubUID}/apps/${app.id}"
        : "${(getFullLocalApiServerUrl() =~ URL_PATTERN).findAll()[0][1]}"
    
    return "${prefix}/${fileName}?access_token=${state.accessToken}&dark=${useDarkTheme == true}"
}

def buildDashboardURL(String dashboardName) {
    String prefix = useCloudLinks == true
        ? "${getApiServerUrl()}/${hubUID}/apps/${app.id}"
        : "${(getFullLocalApiServerUrl() =~ URL_PATTERN).findAll()[0][1]}"
    
    return "${prefix}/watchtower.html?name=${java.net.URLEncoder.encode(dashboardName, 'UTF-8')}&access_token=${state.accessToken}&dark=${useDarkTheme == true}"
}


// ===================================================================================================================
// Metrics handler
// ===================================================================================================================

void collectMetrics() {
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of(location.timeZone.ID)).withSecond(0).withNano(0)
    debug "Collecting metrics: now=${now}, epoch=${now.toEpochSecond()}, doy=${now.getDayOfYear()}, dow=${now.getDayOfWeek()}, hour=${now.getHour()}, min=${now.getMinute()}"
    collectDeviceConfiguration().each { conf ->
        DeviceWrapper device = conf[1]
        List<String> attrs = conf[2]

        update5MinData(now, device, attrs)

        if (now.getMinute() != 0) return
        update1HourData(now, device, attrs)

        if (now.getHour() != 0) return
        update1DayData(now, device, attrs)

        if (now.getDayOfWeek() != DayOfWeek.MONDAY) return
        update1WeekData(now, device, attrs)
    }
}

void update5MinData(ZonedDateTime now, DeviceWrapper device, List<String> attrs) {
    String deviceId = "${device.id}"
    debug "Updating 5 min metrics for ${device} (${deviceId})..."

    // Compute and save a new CSV record
    List<String> newCsvRecord = [ "${now.toEpochSecond()}" ]
    attrs.each { newCsvRecord.add(SUPPORTED_ATTRIBUTES[it].probe(device)) }
    appendDataRecord("wt_${deviceId}_5m.csv", newCsvRecord, attrs, conf_5minMaxLines ?: 864)
}

void update1HourData(ZonedDateTime now, DeviceWrapper device, List<String> attrs) {
    String deviceId = "${device.id}"
    debug "Updating 1 hour metrics for ${device} (${deviceId})..."

    // Compute averages from lower interval file
    String lowerFileName = "wt_${deviceId}_5m.csv"
    Long onlyAfter = now.minusHours(1).toEpochSecond()
    List<BigDecimal> averages = computeAverages(lowerFileName, onlyAfter)
    if (averages.size == 0) return

    // Compute and save a new CSV record
    List<String> newCsvRecord = [ "${now.toEpochSecond()}" ]
    averages.each { newCsvRecord.add("${it.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()}") }
    appendDataRecord("wt_${deviceId}_1h.csv", newCsvRecord, attrs, conf_1HourMaxLines ?: 744)
}

void update1DayData(ZonedDateTime now, DeviceWrapper device, List<String> attrs) {
    String deviceId = "${device.id}"
    debug "Updating 1 day metrics for ${device} (${deviceId})..."

    // Compute averages from lower interval file
    String lowerFileName = "wt_${deviceId}_5m.csv"
    Long onlyAfter = now.minusDays(1).toEpochSecond()
    List<BigDecimal> averages = computeAverages(lowerFileName, onlyAfter)
    if (averages.size == 0) return

    // Compute and save a new CSV record
    List<String> newCsvRecord = [ "${now.toEpochSecond()}" ]
    averages.each { newCsvRecord.add("${it.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()}") }
    appendDataRecord("wt_${deviceId}_1d.csv", newCsvRecord, attrs, conf_1DayMaxLines ?: 732)
}

void update1WeekData(ZonedDateTime now, DeviceWrapper device, List<String> attrs) {
   String deviceId = "${device.id}"
    debug "Updating 1 week metrics for ${device} (${deviceId})..."

    // Compute averages from lower interval file
    String lowerFileName = "wt_${deviceId}_1h.csv"
    Long onlyAfter = now.minusWeeks(1).toEpochSecond()
    List<BigDecimal> averages = computeAverages(lowerFileName, onlyAfter)
    if (averages.size == 0) return

    // Compute and save a new CSV record
    List<String> newCsvRecord = [ "${now.toEpochSecond()}" ]
    averages.each { newCsvRecord.add("${it.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()}") }
    appendDataRecord("wt_${deviceId}_1w.csv", newCsvRecord, attrs, conf_1WeekMaxLines ?: 522)
}

// ===================================================================================================================
// Datastore helpers
// ===================================================================================================================

void appendDataRecord(String fileName, List<String> csvRecord, List<String> attrs, Long maxLines) {
    debug "appendDataRecord(${fileName}, ${csvRecord}, ${attrs}, ${maxLines})"
    List<String> csvLines = []
    csvLines.add("timestamp,${ attrs.join(',') }")
    csvLines.addAll(loadDataLines(fileName, maxLines - 1))
    csvLines.add("${csvRecord.join(',')}")
    uploadHubFile(fileName, csvLines.join("\n").getBytes())
}

List<String> loadDataLines(String fileName, Long maxLines) {
    try {
        return new String(downloadHubFile(fileName), 'UTF-8').trim().split("\n").drop(1).takeRight((int) maxLines)
    } catch (NoSuchFileException ex) {
        warn "Creating data file: ${fileName}"
        return []
    }
}

List<BigDecimal> computeAverages(String fileName, Long onlyAfter) {
    debug "computeAverages(${fileName}, ${onlyAfter})"
    List<String> fileLines = null
    try {
        fileLines = new String(downloadHubFile(fileName), 'UTF-8').trim().split("\n").drop(1)
        if (fileLines.size == 0) return Collections.emptyList()
    } catch (NoSuchFileException ex) {
        warn "computeAverages(${fileName}, ${onlyAfter}): File not found: ${fileName}"
        return Collections.emptyList()
    }

    // Parse and retrieve records of interest
    List<List<BigDecimal>> recordsOfInterest = fileLines
        .collect { line -> line.split(',').collect { new BigDecimal(it) } }
        .findAll { it[0] > onlyAfter }
        .collect { it.tail() }
    int validRecordsNumber = recordsOfInterest.size
    debug "Found ${validRecordsNumber} records of interest: ${recordsOfInterest}"

    // Calculate list with average values
    return recordsOfInterest
        .inject(null) { result, record -> result == null ? record : ([result, record].transpose()*.sum()) }
        .collect { it / validRecordsNumber }
}

void deleteDataFiles(DeviceWrapper device) {
    if (device == null) return
    warn "Deleting data files for ${device} (${device.id})"
    try { deleteHubFile("wt_${device.id}_5m.csv") } catch (NoSuchFileException ex) { }
    try { deleteHubFile("wt_${device.id}_1h.csv") } catch (NoSuchFileException ex) { }
    try { deleteHubFile("wt_${device.id}_1d.csv") } catch (NoSuchFileException ex) { }
    try { deleteHubFile("wt_${device.id}_1w.csv") } catch (NoSuchFileException ex) { }
}
