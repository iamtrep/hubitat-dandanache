import { html, css, LitElement } from '../vendor/vendor.min.js';

export class DashboardMenu extends LitElement {
    static styles = css`
        :host {
            display: block;
            position: fixed;
            top: 0;
            left: 0;
            height: 100%;
        }
        nav {
            position: absolute;
            top: 0;
            left: 0;
            width: 10em;
            height: 100%;
            padding: 5px;
            background-color: var(--bg-color-darker);
            color: var(--text-color);
            box-shadow: 0 0 0.3em var(--shadow-color);
            border-right: 1px var(--border-color) solid;
            animation: showme 1s;
        }
        :host([hidden]) nav {
            animation: hideme 1s;
            left: -500px;
        }
        @keyframes hideme {
            from { left: 0px }
            to { left: -500px }
        }
        @keyframes showme {
            from { left: -500px }
            to { left: 0px }
        }
        hr {
            border: 0;
            border-top: 1px var(--bg-color) solid;
            margin: 10px 0;
        }
        button {
            background-color: transparent;
            color: var(--text-color);
            border: 1px var(--border-color) solid;
            border-radius: 5px;
            margin-bottom: 5px;
            padding: .4em .6em;
            cursor: pointer;
            display: block;
            width: 100%;
            text-align: left;
        }
        button:hover {
            background-color: var(--bg-color);
            box-shadow: 0 0 0.3em var(--shadow-color);
        }
        label {
            display: block;
            margin-bottom: 5px;
            font-size: .85rem;
        }
        select {
            display: block;
        }
        select {
            display: block;
            width: 100%;
            margin-bottom: 5px;
            background-color: var(--bg-color);
            color: var(--text-color);
            border: 1px var(--border-color) solid;
            padding: .5em;
        }
    `;

    static properties = {
        hidden: { type: Boolean, reflect: true },
        refreshInterval: { type: String, state: true },
    }

    constructor() {
        super()
        this.hidden = true
        this.refreshInterval = '0'
    }

    render() {
        return html`
            <nav>
                <button @click=${this.addPanel}>+ Add panel</button>
                <button @click=${this.saveDashboard}>✓ Save dashboard</button>
                <hr>
                <label for="refreshInterval">Auto-refresh</label>
                <select id="refreshInterval" .value=${this.refreshInterval} @change=${this.changeRefreshInterval}>
                    <option value="0">no refresh</option>
                    <option value="5">every 5 minutes</option>
                    <option value="10">every 10 minutes</option>
                    <option value="30">every 30 minutes</option>
                    <option value="60">every hour</option>
                </select>
            </nav>
        `;
    }

    connectedCallback() {
        super.connectedCallback();
        window.addEventListener('keydown', event =>  event.key === '`' && (this.hidden = !this.hidden));
    }

    addPanel() {
        this.dispatchEvent(new CustomEvent('add'))
    }

    saveDashboard() {
        this.dispatchEvent(new CustomEvent('save', { detail: { refresh: this.refresh }}))
    }

    changeRefreshInterval(event) {
        this.refreshInterval = event.target.value
        this.dispatchEvent(new CustomEvent('changeRefreshInterval', { detail: this.refreshInterval }))
    }
}
