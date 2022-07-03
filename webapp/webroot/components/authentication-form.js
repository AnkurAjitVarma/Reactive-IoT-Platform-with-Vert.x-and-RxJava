import {Style} from '../js/helper.js'

const template = document.createElement('template')
template.innerHTML = `
  <section class="hero is-small is-primary">

    <div>
        <div class="field is-horizontal mt-2">
          <div class="control mr-2">
            <input class="input is-primary" name="username" type="text" placeholder="username">
          </div>
          <div class="control mr-2">
            <input class="input is-primary" name="password" type="password" placeholder="password">
          </div>
          <div class="control mr-2">
            <h3 class="title is-3" name="message">Please, Log in</h3>
          </div>
        </div>
        <button class="button is-dark mt-2" name="btnConnect">Connect</button>
        <button class="button is-dark mt-2" name="btnDisConnect">DisConnect</button>
    </div>

  </section>
`

class AuthenticationForm extends HTMLElement {
  constructor() {
    super()
    this.attachShadow({mode: 'open'})
    this.shadowRoot.appendChild(template.content.cloneNode(true))
    this.shadowRoot.adoptedStyleSheets = [Style.sheets.bulma] // you can add several sheets
  }

  getUser() {
    let formData = new FormData()
    formData.append('username', this.fieldUserName.value)
    formData.append('password', this.fieldPassword.value)
    return formData
  }

  connectedCallback() {
    let btnConnect= this.shadowRoot.querySelector(`[name="btnConnect"]`)
    let btnDisConnect= this.shadowRoot.querySelector(`[name="btnDisConnect"]`)

    this.fieldUserName = this.shadowRoot.querySelector(`[name="username"]`)
    this.fieldPassword = this.shadowRoot.querySelector(`[name="password"]`)
    this.authenticationMessage = this.shadowRoot.querySelector(`[name="message"]`)

    btnConnect.addEventListener('click',
     event =>
     {
        fetch('/authenticate',
        {
          method: 'POST',
          body: this.getUser()
        })
        .then(response =>
         {
            if(response.ok)
            {
                        this.dispatchEvent(new CustomEvent('is-authenticated',
                        {
                          bubbles: true, composed: true, detail:
                          {
                            name: this.fieldUserName.value
                          }
                        })
                      )
                      this.fieldUserName.value = ""
                      this.fieldPassword.value = ""
                      this.authenticationMessage.innerHTML = "Logged in"
            }
            else
            {
                this.authenticationMessage.innerHTML = `Login attempt unsuccessful, try again.`
            }
         })
     });

     btnDisConnect.addEventListener('click', event =>
     {
          fetch('/disconnect', {
                  method: 'GET',
                })
                .then(response =>
                {
                console.log(response)
                this.authenticationMessage.innerHTML = `Logged out`
                 this.dispatchEvent(
                                                                        new CustomEvent('is-disconnected', {
                                                                          bubbles: true, composed: true, detail: {}
                                                                        })
                                                                      )
                    })
                .catch(error => console.log(error))
     })


  }
 }

customElements.define('authentication-form', AuthenticationForm)
