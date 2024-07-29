import { html, css, LitElement, nothing } from '../vendor/vendor.min.js';

import { DatastoreHelper } from '../helpers/datastore-helper.js';
import { ColorHelper } from '../helpers/color-helper.js'
import { ChartHelper } from '../helpers/chart-helper.js'

export class DevicePanel extends LitElement {
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
    `;

    static properties = {
        config: { type: Object, reflect: true },
        mobileView: { type: Boolean, state: true },
        chart: { type: Object, state: true },
        nodata: { type: Boolean, state: true },
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
        const supportedAttributes = await DatastoreHelper.fetchSupportedAttributes()
        const data = await DatastoreHelper.fetchDeviceData(this.config.dev, this.config.attr1, this.config.attr2, this.config.precision)
        this.nodata = data.attr1.length == 0

        const datasets = [{
            label: this.config.attr1.charAt(0).toUpperCase() + this.config.attr1.slice(1),
            data: data.attr1,
            pointStyle: false,
            backgroundColor: colors.Green + '44',
            borderColor: colors.Green,
            borderWidth: 1.2,
            tension: 0.5,
            fill: 'start',
            yAxisID: 'attr1',
            unit: supportedAttributes[this.config.attr1].unit
        }]

        this.chart.options.scales.attr1 = {
            position: 'left',
            display: true,
            title: {
                display: true,
                text: `${datasets[0].label} ${supportedAttributes[this.config.attr1].unit}`,
                color: colors.Green
            },
            ticks: { color: colors.TextColorDarker },
            grid: { color: colors.TextColorDarker + '44' },
            suggestedMin: supportedAttributes[this.config.attr1].min,
            suggestedMax: supportedAttributes[this.config.attr1].max
        }

        if (this.config.attr2 !== undefined) {
            datasets.push({
                label: this.config.attr2.charAt(0).toUpperCase() + this.config.attr2.slice(1),
                data: data.attr2,
                pointStyle: false,
                backgroundColor: colors.Blue + '44',
                borderColor: colors.Blue,
                borderWidth: 1.2,
                tension: 0.5,
                fill: 'start',
                yAxisID: 'attr2',
                unit: supportedAttributes[this.config.attr2].unit
            })

            this.chart.options.scales.attr2 = {
                position: 'right',
                display: true,
                title: {
                    display: true,
                    text: `${datasets[1].label} ${supportedAttributes[this.config.attr2].unit}`,
                    color: colors.Blue
                },
                ticks: { color: colors.TextColorDarker },
                grid: { drawOnChartArea: false },
                min: supportedAttributes[this.config.attr2].min,
                max: supportedAttributes[this.config.attr2].max
            }
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
                            limits: { x: { min: 'original', max: 'original' }},
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
        const data = await DatastoreHelper.fetchDeviceData(this.config.dev, this.config.attr1, this.config.attr2, this.config.precision)
        console.log('refresh data', data)
        this.nodata = data.attr1.length == 0
        this.chart.data.datasets[0].data = data.attr1
        if (this.config.attr2 !== undefined) this.chart.data.datasets[1].data = data.attr2
        this.chart.config.type = data.attr1.length < 10 ? 'bar' : 'line'
        this.chart.update('none')
        ChartHelper.updateChartType(this.chart)
        this.classList.remove('spinner')
    }

    decorateConfig(config) {
        return { ...config, ...this.config }
    }
}

export class DevicePanelConfig extends LitElement {
    static properties = {
        devices: { type: Object, state: true },
        attributes: { type: Object, state: true },

        device: { type: String, state: true },
        attr1: { type: String, state: true },
        attr2: { type: String, state: true },
    }

    constructor() {
        super()
        this.devices = undefined
        this.attributes = undefined

        this.dev = undefined
        this.attr1 = undefined
        this.attr2 = undefined
    }

    render() {
        return html`
            <label for="device">Select device:</label>
            ${this.devices ? this.renderDevicesSelect() : html`<aside class="spinner">Loading devices ...</aside>`}
            ${this.attributes ? this.renderAttributesSelect() : nothing }
        `
    }

    connectedCallback() {
        super.connectedCallback()
        DatastoreHelper.fetchMonitoredDevices().then(devices => {
            this.devices = devices
        })
    }

    createRenderRoot() {
        return this
    }

    renderDevicesSelect() {
        setTimeout(() => this.renderRoot.querySelector('#device').focus(), 0)
        return html`
            <section>
                <select id="device" .value=${this.dev} @change=${this.onDeviceSelect} required="true">
                    <option value=""></option>
                    ${this.devices.map(device => html`
                        <option value="${device.id}" .selected=${this.dev === device.id}>${device.name}</option>
                    `
                    )}
                </select>
            </section>
        `
    }

    renderAttributesSelect() {
        setTimeout(() => this.renderRoot.querySelector('#attr1').focus(), 0)
        return html`
            <section>
                <label for="attr1">Select attribute to chart:</label>
                <select id="attr1" .value=${this.attr1} @change=${event => this.attr1 = event.target.value} required="true">
                    <option value=""></option>
                    ${this.attributes.filter(attribute => attribute != this.attr2).map(attribute => html`
                        <option value="${attribute}" .selected=${this.attr1 === attribute}>${attribute}</option>
                    `
                    )}
                </select>
            </section>
            ${ this.attr1 !== undefined && this.attributes.length > 1 ? this.renderOptionalAttributesSelect() : nothing }
        `
    }

    renderOptionalAttributesSelect() {
        setTimeout(() => this.renderRoot.querySelector('#attr2').focus(), 0)
        return html`
            <section>
                <label for="attr2">Select additional attribute:</label>
                <select id="attr2" .value=${this.attr2} @change=${event => this.attr2 = event.target.value}>
                    <option value="">[optional]</option>
                    ${this.attributes.filter(attribute => attribute != this.attr1).map(attribute => html`
                        <option value="${attribute}" .selected=${this.attr2 === attribute}>${attribute}</option>
                    `
                    )}
                </select>
            </section>
        `
    }

    onDeviceSelect(event) {
        this.dev = event.target.value !== '' ? event.target.value : undefined
        this.attributes = this.dev !== undefined ? this.devices.find(device => device.id == this.dev).attrs.sort() : undefined
    }

    decorateConfig(config) {
        return { ...config, dev: this.dev, attr1: this.attr1, attr2: this.attr2, precision: '5m' }
    }
}
