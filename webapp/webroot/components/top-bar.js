import {Style} from '../js/helper.js'

import './authentication-form.js'

const template = document.createElement('template')
template.innerHTML = `
  <section class="hero is-small is-primary">
    <div class="hero-body">
      <p class="title">SmartHome Console</p>
      <p class="subtitle">
        in action ...
      </p>
    </div>
    <div class="hero-body">
        <authentication-form></authentication-form>
    </div>
  </section>
`

class TopBar extends HTMLElement {
  constructor() {
    super()
    this.attachShadow({mode: 'open'})
    this.shadowRoot.appendChild(template.content.cloneNode(true))
    this.shadowRoot.adoptedStyleSheets = [Style.sheets.bulma] // you can add several sheets
  }

}

customElements.define('top-bar', TopBar)
