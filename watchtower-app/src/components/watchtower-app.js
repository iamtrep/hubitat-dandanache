import { html, css, LitElement } from '../vendor/vendor.min.js';
import { DatastoreHelper } from '../helpers/datastore-helper.js'

export class WatchtowerApp extends LitElement {
    static styles = css`
        :host {
            display: block;
            height: calc(100vh - 10px);
        }
    `

    constructor() {
        super()
        this.params = new URLSearchParams(window.location.search)
        this.name = this.params.get('name')
        if (this.name === null) {
            alert('Query parameter [name] is missing!')
            throw new Error('Query parameter [name] is missing!')
        }
    }

    render() {
        return html`
            <dashboard-grid name=${this.name}></dashboard-grid>
            <dashboard-menu
                @add=${this.showAddDialog}
                @compact=${this.compactPanels}
                @changeRefreshInterval=${this.changeRefreshInterval}
                @save=${this.saveDashboard}
            ></dashboard-menu>
            <dashboard-add-dialog @done=${this.addDashboardPanel}></dashboard-add-dialog>
        `
    }

    connectedCallback() {
        super.connectedCallback()
        document.body.classList.remove('spinner')
    }

    async firstUpdated() {
        const menuElm = this.renderRoot.querySelector('dashboard-menu')
        const gridElm = this.renderRoot.querySelector('dashboard-grid')

        const layout = await DatastoreHelper.fetchGridLayout(this.params.get('name'));
        const refreshInterval = layout.refresh ? parseInt(layout.refresh) : 0
        const theme = layout.theme === 'dark' ? 'dark' : 'light'

        // Show menu if dashboard contains no panels
        if (layout.panels.length === 0) menuElm.open = true

        // Init grid
        await gridElm.updateComplete
        gridElm.init(layout.panels)
        gridElm.setRefreshInterval(refreshInterval)

        // Update menu
        menuElm.refreshInterval = `${refreshInterval}`
        menuElm.setTheme(theme)
    }

    async saveDashboard() {
        const menuElm = this.renderRoot.querySelector('dashboard-menu')
        const layout = {
            refresh: menuElm.refreshInterval,
            theme: menuElm.theme,
            panels: this.renderRoot.querySelector('dashboard-grid').getPanelsConfig()
        }
        console.info('Saving dashboard to Hubitat', this.name, layout)
        await DatastoreHelper.saveGridLayout(this.name, layout)
    }

    showAddDialog() {
        this.renderRoot.querySelector('dashboard-add-dialog').setAttribute('open', true)
    }

    compactPanels() {
        this.renderRoot.querySelector('dashboard-grid').compact()
    }

    addDashboardPanel(event) {
        this.renderRoot.querySelector('dashboard-grid').addPanel(event.detail)
    }

    changeRefreshInterval(event) {
        const refreshInterval = parseInt(event.detail)
        this.renderRoot.querySelector('dashboard-grid').setRefreshInterval(refreshInterval)
    }
}
