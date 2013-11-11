$(function () {

    $('#spline-adds').highcharts({
        chart: {
            zoomType: 'x',
            type: 'spline'
        },
        title: {
            text: 'Lines of Code Over Time (Team)'
        },
        xAxis: {
            type: 'datetime'
        },
        yAxis: {
            title: {
                text: 'Lines of Code'
            }
        },
        series: []
    });

    $('#pie-adds').highcharts({
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false
            },
            title: {
                text: 'Total Lines of Code (Team)'
            },
            tooltip: {
                pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: true,
                        color: '#000000',
                        connectorColor: '#000000',
                        format: '<b>{point.name}</b>: {point.percentage:.1f} %'
                    },
                    showInLegend: true
                }
            },
            series: [{
                type: 'pie',
                name: 'Lines of Code',
                data: []
            }]
        });

    $('#spline-adds-author').highcharts({
        chart: {
            zoomType: 'x',
            type: 'spline'
        },
        title: {
            text: 'Lines of Code Over Time (User)'
        },
        xAxis: {
            type: 'datetime'
        },
        yAxis: {
            title: {
                text: 'Lines of Code'
            }
        },
        series: []
    });

    $('#pie-adds-author').highcharts({
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false
            },
            title: {
                text: 'Total Lines of Code (User)'
            },
            tooltip: {
                pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: true,
                        color: '#000000',
                        connectorColor: '#000000',
                        format: '<b>{point.name}</b>: {point.percentage:.1f} %'
                    }
                },
                showInLegend: true
            },
            series: [{
                type: 'pie',
                name: 'Lines of Code',
                data: []
            }]
        });


    var wsUri = window.location.href.replace(/^http(s?:\/\/.*)\/.*$/, 'ws$1/event-stream');
    console.log("Connecting to websocket at: " + wsUri);
    var socket = new WebSocket(wsUri);

    socket.onmessage = function(event) {
        var commits = JSON.parse(event.data);
        var redraw = commits.length <= 1
        console.log(commits);
        for (i in commits) {
            updateCharts(commits[i], redraw);
        }
        if (!redraw) {
            redrawCharts();
        }
    };
});

var updateCharts = function(commit, redraw) {
    updateSpline(commit, $("#spline-adds"), commit.team, redraw)
    updateSpline(commit, $("#spline-adds-author"), commit.author, redraw)
    updatePie(commit, $("#pie-adds"), commit.team, redraw)
    updatePie(commit, $("#pie-adds-author"), commit.author, redraw)
}

var redrawCharts = function() {
    $("#spline-adds").highcharts().redraw();
    $("#spline-adds-author").highcharts().redraw();
    $("#pie-adds").highcharts().redraw();
    $("#pie-adds-author").highcharts().redraw();
}

var updateSpline = function (commit, element, name, redraw) {
    var chart = element.highcharts();
    var series = find(chart.series, function(s) { return s.name === name });
    var totalDataPoints = chart.series.reduce(function(a,b) { return a + b.data.length }, 0);
    var shift = totalDataPoints > 50; // TODO: Find oldest series and remove point

    if(series) {
        series.addPoint([commit.ts, commit.additions], redraw);
    } else {
        chart.addSeries({
            name: name,
            data: [[commit.ts, commit.additions]]
        }, redraw);
    }

    console.log("total: " + totalDataPoints + ", shift: " + shift);
    if(shift) {
        var oldestDataPoint = chart.series.reduce(function(a,b) { return a && a.x < b.data[0].x ? a : b.data[0] }, chart.series[0].data[0]);
        oldestDataPoint.remove(redraw);
    }
}

var updatePie = function (commit, element, name, redraw) {
    var chart = element.highcharts();
    var series = chart.series[0];
    var dataPoint = find(series.data, function(d) { return d.name == name});
    if(dataPoint) {
        dataPoint.update(dataPoint.y + commit.additions, redraw);
    } else {
        series.addPoint([name, commit.additions], redraw);
    }
}

var find = function (arr, pred) {
    for (i in arr) {
        if(pred(arr[i])) {
            return arr[i];
        }
    }
    return null;
}