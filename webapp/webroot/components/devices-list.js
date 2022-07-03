import {Style} from '../js/helper.js'

import './device-card.js'

const template = document.createElement('template')
template.innerHTML = `
<div>
    <h2 class="title is-2">
      Devices
    </h2>
    <div class="columns is-multiline">

    </div>
</div>
`

class DevicesList extends HTMLElement {
  constructor() {
    super()
    this.attachShadow({mode: 'open'})
    this.shadowRoot.appendChild(template.content.cloneNode(true))
    this.shadowRoot.adoptedStyleSheets = [Style.sheets.bulma] // you can add several sheets

    this.devices = {}
  }

  connectedCallback() {
    this.columns = this.shadowRoot.querySelector(".columns")
  }

  // this is called when a new device is connected
  newDevice(deviceId, device) {
    console.log("🌤 new device is connected", deviceId, device)

    let deviceCard = document.createElement("device-card")

    this.columns.appendChild(deviceCard)
    deviceCard.setTitle(`${device.topic}/[${device.category}]/${deviceId}`)

    deviceCard.setDeviceInfo(`
      <ul>
        <li>Location: ${device.location}</li>
        <li>${device.date}/${device.hour}</li>
      </ul>
    `)
    this.devices[deviceId] = deviceCard

  }

  // this is called when the metrics are updated
  newMetrics(deviceId, device) {

    this.devices[deviceId].setDeviceInfo(`
      <ul>
        <li>Location: ${device.location}</li>
        <li>${device.date}/${device.hour}</li>
      </ul>
    `)

    this.devices[deviceId].setTemperatureInfo(device.sensors.temperatureMetrics)
    this.devices[deviceId].setHumidityInfo(device.sensors.humidityMetrics)
    this.devices[deviceId].setECo2Info(device.sensors.eCO2Metrics)

  }

}

customElements.define('devices-list', DevicesList)
