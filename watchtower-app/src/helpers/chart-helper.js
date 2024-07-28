import '../vendor/vendor.min.js'

export class ChartHelper {
    static crosshairPlugin() {
        return {
            id: 'crosshair',
            defaults: {
                width: 1,
                color: 'red',
                dash: [2, 2]
            },
            afterInit: (chart, args, opts) => {
                chart.crosshair = {
                    x: 0,
                    y: 0
                }
            },
            afterEvent: (chart, args) => {
                const { inChartArea } = args
                const { type, x, y } = args.event

                chart.crosshair = { x, y, draw: inChartArea }
                chart.draw()
            },
            beforeDatasetsDraw: (chart, args, opts) => {
                const { ctx } = chart
                const { top, bottom, left, right } = chart.chartArea
                const { x, y, draw } = chart.crosshair
                if (!draw) return

                ctx.save()
                ctx.beginPath()
                ctx.lineWidth = opts.width
                ctx.strokeStyle = opts.color
                ctx.setLineDash(opts.dash)
                ctx.moveTo(x, bottom)
                ctx.lineTo(x, top)
                ctx.stroke()
                ctx.restore()
            }
        }
    }

    static updatePointStyle(chart) {
        if (chart.scales.x === undefined || chart.data.datasets[0] === undefined) return
        const min = chart.scales.x.min
        const max = chart.scales.x.max
        const visibleDatapoints = chart.data.datasets[0].data.filter(point => point.x >= min && point.x <= max)
        const chartType = visibleDatapoints.length == 0 || chart.width / visibleDatapoints.length < 30 ? 'line' : 'bar'
        if (chart.config.type != chartType) {
            chart.config.type = chartType
            chart.update('none')
        }
        return

        // let shouldUpdateChart = false
        // const newPointStyle = visibleDatapoints.length == 0 || chart.width / visibleDatapoints.length < 10 ? false : 'circle'
        // chart.data.datasets.forEach(dataset => {
        //     if (newPointStyle !== dataset.pointStyle) {
        //         dataset.pointStyle = newPointStyle
        //         shouldUpdateChart = true
        //     }
        // })
        // if (shouldUpdateChart) chart.update()
    }
}
