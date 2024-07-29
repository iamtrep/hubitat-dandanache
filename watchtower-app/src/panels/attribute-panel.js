import { html, css, LitElement, nothing } from '../vendor/vendor.min.js';

import { DatastoreHelper } from '../helpers/datastore-helper.js';
import { ColorHelper } from '../helpers/color-helper.js'
import { ChartHelper } from '../helpers/chart-helper.js'

export class AttributePanel extends LitElement {
    static styles = css`
       :host {
            display: block;
            width: 100%;
            height: 100%;
        }
        canvas {
            width: 100%;
            height: 100%;
        }
        :host(.empty) canvas { visibility: hidden }
        precision-selector {
            position: absolute;
            bottom: 0;
            left: 50%;
            transform: translate(-50%, 0);
            visibility: hidden;
        }
        :host(:hover) precision-selector {
            visibility: visible;
        }
        aside {
            position: absolute;
            top: 50%;
            left: 0;
            transform: translate(0, -50%);
            display: block;
            color: var(--text-color-darker);
            width: 100%;
            text-align: center;
        }
    `

    static properties = {
        config: { type: Object, reflect: true },
        mobileView: { type: Boolean, state: true },
        chart: { type: Object, state: true },
        nodata: { type: Boolean, state: true }
    }

    render() {
        return html`
            <canvas></canvas>
            <precision-selector @change=${this.changePrecision} .precision=${this.config.precision}></precision-selector>
            ${ this.nodata === true ? html`<aside>No data yet</aside>` : nothing}
        `;
    }

    updated(changedProperties) {
        if (changedProperties.mobileView == this.mobileView) return
        this.chart.options.plugins.zoom.pan.enabled = !this.mobileView
        this.chart.options.plugins.zoom.zoom.pinch.enabled = !this.mobileView
    }

    async connectedCallback() {
        super.connectedCallback()

        if (this.config.precision === undefined) this.config.precision = '5m'

        const colors = ColorHelper.colors()
        const graphColors = ColorHelper.graphColors()
        const supportedAttributes = await DatastoreHelper.fetchSupportedAttributes()
        const monitoredDevices = await DatastoreHelper.fetchMonitoredDevices()
        const data = await DatastoreHelper.fetchAttributeData(this.config.attr, this.config.devs, this.config.precision)
        //this.nodata = data.attr1.length == 0

        const datasets = []
        let idx = 0
        for (const deviceId of this.config.devs) {
            const color = graphColors[idx++]
            datasets.push({
                label: monitoredDevices.find(monitoredDevice => monitoredDevice.id == deviceId).name,
                data: data[`dev_${deviceId}`],
                pointStyle: false,
                backgroundColor: color ? `${color}44` : undefined,
                borderColor: color ? color : undefined,
                borderWidth: 1.2,
                tension: 0.5,
                fill: 'start',
                unit: supportedAttributes[this.config.attr].unit,
                ref: `dev_${deviceId}`
            })
        }

        const attrLabel = this.config.attr.charAt(0).toUpperCase() + this.config.attr.slice(1)
        this.chart.options.scales.y = {
            position: 'left',
            display: true,
            //beginAtZero: true,
            title: {
                display: true,
                text: `${attrLabel} ${supportedAttributes[this.config.attr].unit}`,
                color: colors.Green
            },
            ticks: { color: colors.TextColorDarker },
            grid: { color: colors.TextColorDarker + '44' },
            suggestedMin: supportedAttributes[this.config.attr].min,
            suggestedMax: supportedAttributes[this.config.attr].max
        }

        this.chart.data = { datasets }
        ChartHelper.updateChartType(this.chart)
        this.chart.update('none')
        setTimeout(() => this.classList.remove('empty', 'spinner'), 200)
    }

    firstUpdated() {
        const colors = ColorHelper.colors()
        this.chart = new Chart(
            this.renderRoot.querySelector('canvas'),
            {
                type: 'line',
                options: {
                    parsing: false,
                    normalized: true,
                    responsive: true,
                    maintainAspectRatio: false,
                    onResize: chart => ChartHelper.updateChartType(chart),
                    animation: { duration: 0, onComplete: ({ initial, chart }) => (initial ? ChartHelper.updateChartType(chart) : undefined) },
                    layout: { padding: { top: 20, bottom: 3 }},
                    stacked: false,
                    pointStyle: false,
                    scales: {
                        x: {
                            type: 'time',
                            time: {
                                minUnit: 'minute',
                                displayFormats: {
                                    minute: 'd LLL HH:mm',
                                    hour: 'd LLL HH:mm',
                                    day: 'd LLL'
                                },
                                tooltipFormat: 'd LLL HH:mm'
                            },
                            title: { display: false },
                            ticks: {
                                color: colors.TextColorDarker,
                                maxRotation: 0,
                                autoSkipPadding: 15
                            },
                            grid: { color: colors.TextColorDarker + '44' }
                        }
                    },
                    interaction: {
                        mode: 'nearest',
                        axis: 'x',
                        intersect: false
                    },
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            itemSort: (a, b) => b.raw.y - a.raw.y,
                            callbacks: { label: t => ` ${t.dataset.label}: ${t.parsed.y}${t.dataset.unit}` },
                            backgroundColor: colors.BgColorDarker,
                            titleColor: colors.TextColor,
                            bodyColor: colors.TextColorDarker,
                            borderColor: colors.BorderColor,
                            borderWidth: 1
                        },
                        decimation: { enabled: true, algorithm: 'lttb' },
                        zoom: {
                            pan: { enabled: this.mobileView !== true, mode: 'x' },
                            zoom: {
                                wheel: { enabled: true },
                                pinch: { enabled: this.mobileView !== true },
                                mode: 'x',
                                onZoomComplete: ({ chart }) => ChartHelper.updateChartType(chart)
                            },
                            limits: { x: { min: 'original', max: 'original' }}
                        },
                        crosshair: { color: colors.TextColor }
                    }
                },
                plugins: [ ChartHelper.crosshairPlugin() ]
            }
        )
        this.chart.canvas.style.touchAction = 'pan-y'
    }

    async changePrecision(event) {
        this.config.precision = event.detail
        await this.refresh()
        this.chart.resetZoom()
    }

    async refresh() {
        this.classList.add('spinner')
        const data = await DatastoreHelper.fetchAttributeData(this.config.attr, this.config.devs, this.config.precision)
        //this.nodata = data.attr1.length == 0
        this.chart.data.datasets.forEach(dataset => dataset.data = data[dataset.ref])
        this.chart.update('none')
        ChartHelper.updateChartType(this.chart)
        this.classList.remove('spinner')
    }

    decorateConfig(config) {
        return { ...config, ...this.config }
    }
}

export class AttributePanelConfig extends LitElement {
    static properties = {
        devices: { type: Object, state: true },
        attributes: { type: Object, state: true },

        attr: { type: String, state: true },
        devs: { type: Object, state: true },
    }

    constructor() {
        super()
        this.devices = undefined
        this.attributes = undefined

        this.attr = undefined
        this.devs = []
    }

    render() {
        return html`
            <label for="device">Select attribute to chart:</label>
            ${this.attributes ? this.renderAttributesSelect() : html`<aside class="spinner">Loading devices ...</aside>`}
            ${this.attr ? this.renderDevicesSelect() : nothing }
        `
    }

    connectedCallback() {
        super.connectedCallback()
        DatastoreHelper.fetchMonitoredDevices().then(devices => {
            this.devices = devices

            const attrs = new Set()
            devices.forEach(device => device.attrs.forEach(attr => attrs.add(attr)))
            this.attributes = [...attrs].sort()
        })
    }

    createRenderRoot() {
        return this
    }

    renderAttributesSelect() {
        setTimeout(() => this.renderRoot.querySelector('#attr').focus(), 0)
        return html`
            <section>
                <select id="attr" .value=${this.attr} @change=${this.onAttributeSelect} required="true">
                    <option value=""></option>
                    ${this.attributes.map(attribute => html`
                        <option value="${attribute}" .selected=${this.attr === attribute}>${attribute}</option>
                    `
                    )}
                </select>
            </section>
        `
    }

    renderDevicesSelect() {
        const devices = this.devices.filter(device => device.attrs.includes(this.attr))
        return html`
            <section>
                <label>Select devices (at least one):</label>
                ${devices.map(device => {
                    return html`<label><input value="${device.id}" type="checkbox"
                        required=${this.devs.length == 0 ? 'yes' : nothing}
                        @change=${this.onDeviceSelect}
                    > ${device.name}</label>`
                })}
            </section>
        `
    }

    onAttributeSelect(event) {
        this.attr = event.target.value !== '' ? event.target.value : undefined
        this.devs = []
        let suggestedTitle = this.attr.replace(/([A-Z])(?=[A-Z][a-z])|([a-z])(?=[A-Z])/g, '$& ')
        suggestedTitle = suggestedTitle[0].toUpperCase() + suggestedTitle.slice(1);
        if (this.attr) this.dispatchEvent(new CustomEvent('suggestTitle', { detail: suggestedTitle }))
    }

    onDeviceSelect() {
        this.devs = [...this.renderRoot.querySelectorAll('input[type="checkbox"]:checked')].map(input => input.value)
    }

    decorateConfig(config) {
        return { ...config, attr: this.attr, devs: this.devs }
    }
}
