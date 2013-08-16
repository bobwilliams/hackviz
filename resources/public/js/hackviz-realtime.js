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
                    }
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
                }
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
        console.log(commits);
        for (i in commits) {
            updateCharts(commits[i]);
        }
    };
});

var updateCharts = function(commit) {
    updateSpline(commit, $("#spline-adds"), commit.team)
    updateSpline(commit, $("#spline-adds-author"), commit.author)
    updatePie(commit, $("#pie-adds"), commit.team)
    updatePie(commit, $("#pie-adds-author"), commit.author)
}


var updateSpline = function (commit, element, name) {
    var chart = element.highcharts();
    var series = find(chart.series, function(s) { return s.name === name });
    if(series) {
        series.addPoint([commit.time, commit.additions]);
    } else {
        chart.addSeries({
            name: name,
            data: [[commit.time, commit.additions]]
        });
    }
}

var updatePie = function (commit, element, name) {
    var chart = element.highcharts();
    var series = chart.series[0];
    var dataPoint = find(series.data, function(d) { return d.name == name});
    if(dataPoint) {
        dataPoint.update(dataPoint.y + commit.additions);
    } else {
        series.addPoint([name, commit.additions])
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