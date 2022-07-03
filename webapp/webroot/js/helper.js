async function loadCssRawContent(cssPath) {
  try {
    // load the css file
    const response = await fetch(cssPath, {
      mode: 'no-cors',
      credentials: 'same-origin' // include, *same-origin, omit
    })
    const cssRaw = await response.text()
    return cssRaw
  } catch(error) {
    return error
  }
}


let Style = {
  sheets: {},
  loadCss: (cssPath, cssName) => loadCssRawContent(cssPath).then(cssRawContent => {
    // create a CSSStyleSheet from the content of the css file
    const sheet = new CSSStyleSheet()
    sheet.replaceSync(cssRawContent)
    // save the css result to a global variable to share styles
    // with web components
    Style.sheets[cssName] = sheet
    // apply the styles to the body of the page
    sheet.applyToAllDocument = true
    return sheet
  })
}

export {Style}
