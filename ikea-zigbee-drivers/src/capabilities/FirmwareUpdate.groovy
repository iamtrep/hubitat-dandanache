{{!--------------------------------------------------------------------------}}
{{# @commands }}

// Commands for capability.FirmwareUpdate
command "updateFirmware"
{{/ @commands }}
{{!--------------------------------------------------------------------------}}
{{# @implementation }}

// Implementation for capability.FirmwareUpdate
def updateFirmware() {
    Log.info '[IMPORTANT] For battery-powered devices, click the "Update Firmware" button immediately after pushing any button on the device in order to first wake it up!'
    Utils.sendZigbeeCommands(zigbee.updateFirmware())
}
{{/ @implementation }}
{{!--------------------------------------------------------------------------}}
