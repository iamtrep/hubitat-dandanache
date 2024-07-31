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

        this.menuElm = undefined
        this.gridElm = undefined
        this.dialogElm = undefined

        this.params = new URLSearchParams(window.location.search)
        this.name = this.params.get('name')
        if (this.name === null) {
            alert('Query parameter [name] is missing!')
            throw new Error('Query parameter [name] is missing!')
        }

        this.mobileView = window.innerWidth < 768
        window.addEventListener('resize', () => {
            const newState = window.innerWidth < 768
            if (this.mobileView == newState) return
            this.mobileView = newState
            this.applyMobileView()
        })
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
        this.menuElm = this.renderRoot.querySelector('dashboard-menu')
        this.gridElm = this.renderRoot.querySelector('dashboard-grid')
        this.dialogElm = this.renderRoot.querySelector('dashboard-add-dialog')

        const layout = await DatastoreHelper.fetchGridLayout(this.params.get('name'));
        const refreshInterval = layout.refresh ? parseInt(layout.refresh) : 0
        const theme = layout.theme === 'dark' ? 'dark' : 'light'

        // Show menu if dashboard contains no panels
        if (layout.panels.length === 0) this.menuElm.open = true

        // Init grid
        await this.gridElm.updateComplete
        this.gridElm.init(layout.panels)
        this.gridElm.setRefreshInterval(refreshInterval)

        // Update menu
        this.menuElm.refreshInterval = `${refreshInterval}`
        this.menuElm.setTheme(theme)

        // Apply mobile view
        this.applyMobileView()
    }

    applyMobileView() {
        this.gridElm.applyMobileView(this.mobileView)
        this.menuElm.applyMobileView(this.mobileView)
    }

    async saveDashboard() {
        const layout = {
            refresh: this.menuElm.refreshInterval,
            theme: this.menuElm.theme,
            panels: this.gridElm.getPanelsConfig()
        }
        console.info('Saving dashboard to Hubitat', this.name, layout)
        await DatastoreHelper.saveGridLayout(this.name, layout)
    }

    showAddDialog() {
        this.dialogElm.setAttribute('open', true)
    }

    compactPanels() {
        this.gridElm.compact()
    }

    addDashboardPanel(event) {
        this.gridElm.addPanel(event.detail)
    }

    changeRefreshInterval(event) {
        const refreshInterval = parseInt(event.detail)
        this.gridElm.setRefreshInterval(refreshInterval)
    }
}
