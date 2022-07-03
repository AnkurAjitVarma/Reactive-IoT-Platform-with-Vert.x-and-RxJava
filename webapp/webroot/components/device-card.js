import {Style} from '../js/helper.js'

const template = document.createElement('template')
template.innerHTML = `
      <div class="column"> <!-- <div class="column is-one-quarter"> -->
        <div class="card">
          <header class="card-header">
            <p class="card-header-title title is-4" style="height:70px">
              <!-- title -->
            </p>
          </header>
          <div class="card-content">
            <div class="content">
              <!-- content -->
              <div class="device-info"></div>
              <hr>
              <!-- charts -->
              <canvas class="temperature-info"></canvas>
              <canvas class="humidity-info"></canvas>
              <canvas class="eco2-info"></canvas>

            </div> <!-- content -->
          </div> <!-- card-content -->
        </div> <!-- card -->
    </div> <!-- column -->
`

class DeviceCard extends HTMLElement {
  constructor() {
    super()
    this.attachShadow({mode: 'open'})
    this.shadowRoot.appendChild(template.content.cloneNode(true))
    this.shadowRoot.adoptedStyleSheets = [Style.sheets.bulma] // you can add several sheets
  }

  connectedCallback() {
    this.cardTitle = this.shadowRoot.querySelector(".card-header-title")
    this.deviceInfo = this.shadowRoot.querySelector(".device-info")
    this.temperatureInfo = this.shadowRoot.querySelector(".temperature-info")
    this.humidityInfo = this.shadowRoot.querySelector(".humidity-info")
    this.eCo2Info = this.shadowRoot.querySelector(".eco2-info")

    // Initialize and add graphs here
    this.temperatureChartConfig = {
      type: 'line',
      data: {
        labels: ['1','2','3','4','5','6','7','8','9','10'],
        datasets: [{
          label: 'Temperature (last 10 metrics)',
          backgroundColor: 'rgb(255, 99, 132)',
          borderColor: 'rgb(255, 99, 132)',
          data: []
        }]
      },
      options: {
        aspectRatio:4
      }
    }
    this.temperatureChart = new Chart(
      this.temperatureInfo,
      this.temperatureChartConfig
    )

    this.humidityChartConfig = {
      type: 'line',
      data: {
        labels: ['1','2','3','4','5','6','7','8','9','10'],
        datasets: [{
          label: 'Humidity (last 10 metrics)',
          backgroundColor: 'rgb(99,224,255)',
          borderColor: 'rgb(99,224,255)',
          data: []
        }]
      },
      options: {
        aspectRatio:4
      }
    }
    this.humidityChart = new Chart(
      this.humidityInfo,
      this.humidityChartConfig
    )

    this.eCo2ChartConfig = {
      type: 'line',
      data: {
        labels: ['1','2','3','4','5','6','7','8','9','10'],
        datasets: [{
          label: 'ECO2 (last 10 metrics)',
          backgroundColor: 'rgb(164,99,255)',
          borderColor: 'rgb(164,99,255)',
          data: []
        }]
      },
      options: {
        aspectRatio:4
      }
    }
    this.eCo2Chart = new Chart(
      this.eCo2Info,
      this.eCo2ChartConfig
    )
  }

  setTitle(title) {
    this.cardTitle.innerHTML = title
  }

  setDeviceInfo(title) {
    this.deviceInfo.innerHTML = title
  }

  setTemperatureInfo(metrics) {
    this.temperatureChart.data.datasets[0].data = metrics
    this.temperatureChart.update()
  }

  setHumidityInfo(metrics) {
    this.humidityChart.data.datasets[0].data = metrics
    this.humidityChart.update()
  }

  setECo2Info(metrics) {
    this.eCo2Chart.data.datasets[0].data = metrics
    this.eCo2Chart.update()
  }

}

customElements.define('device-card', DeviceCard)
