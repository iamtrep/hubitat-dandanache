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
            <dashboard-menu @add=${this.showAddDialog} @save=${this.saveDashboard} @changeRefreshInterval=${this.changeRefreshInterval}></dashboard-menu>
            <dashboard-add-dialog @done=${this.addDashboardPanel}></dashboard-add-dialog>
        `
    }

    connectedCallback() {
        super.connectedCallback()
        const params = new URLSearchParams(window.location.search)
        document.documentElement.setAttribute('data-theme', this.params.get('dark') === 'true' ? 'dark' : 'light')
        document.body.classList.remove('spinner')
    }

    async firstUpdated() {
        const menuElm = this.renderRoot.querySelector('dashboard-menu')
        const gridElm = this.renderRoot.querySelector('dashboard-grid')

        const layout = await DatastoreHelper.fetchGridLayout(this.params.get('name'));

        // Show menu if dashboard contains no panels
        if (layout.panels.length === 0) menuElm.removeAttribute('hidden')

        // Init grid
        gridElm.init(layout.panels)

        // Update auto-refresh
        const refreshInterval = layout.refresh ? parseInt(layout.refresh) : 0
        menuElm.refreshInterval = `${refreshInterval}`
        gridElm.setRefreshInterval(refreshInterval)
    }

    async saveDashboard() {
        const layout = {
            refresh: this.renderRoot.querySelector('dashboard-menu').refreshInterval,
            panels: this.renderRoot.querySelector('dashboard-grid').getPanelsConfig()
        }
        console.log('saveDashboard', this.name, layout)
        await DatastoreHelper.saveGridLayout(this.name, layout)
    }

    showAddDialog() {
        this.renderRoot.querySelector('dashboard-add-dialog').setAttribute('open', true)
    }

    addDashboardPanel(event) {
        this.renderRoot.querySelector('dashboard-grid').addPanel(event.detail)
    }

    changeRefreshInterval(event) {
        const refreshInterval = parseInt(event.detail)
        this.renderRoot.querySelector('dashboard-grid').setRefreshInterval(refreshInterval)
    }
}
