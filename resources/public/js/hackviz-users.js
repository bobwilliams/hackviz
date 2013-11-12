var createHourData = function(field) {
    return function(hourResults) {
        return [
            moment(hourResults.group, "YYYYMMDDHHmm").unix()*1000,
            hourResults.data[0].data[0][field]
        ];
    };
};

var createTeamData = function(field) {
    return function(teamResults) {
        return {
            name: teamResults.group,
            data: teamResults.data.map(createHourData(field))
        };
    };
};

var commitValue = function(commitData) {
    return commitData[1];
}

var createPieData = function(teamSeries) {
    return [
        teamSeries.name,
        teamSeries.data.map(commitValue).reduce(function(a,b) {return a+b})
    ];
};

var createMatches = function(team) {
    if(team && !(team === "All Teams")) {
        return [createMatch("team", "eq", team)];
    } else {
        return [];
    }
};

var renderAuthorCommits = function(team) {
    var authorCommitQuery = makeQuery(createMatches(team), [createSegmentGroup("author"), createDurationGroup("minute")], [createReducer("repo","count")])
    $.getJSON('query?q=' + toEncodedJson(authorCommitQuery), function(data) {
        var series = data.results.map(createTeamData("repo-count"));
        var pieSeries = series.map(createPieData);

        $('#spline-commits-author').highcharts({
            chart: {
                zoomType: 'x',
                type: 'spline'
            },
            title: {
                text: 'Commits Over Time'
            },
            xAxis: {
                type: 'datetime'
            },
            yAxis: {
                title: {
                    text: 'Commits'
                }
            },
            series: series
        });

        $('#pie-commits-author').highcharts({
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false
            },
            title: {
                text: 'Total Commits'
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
                name: 'Commits',
                data: pieSeries
            }]
        });
    });
};

var renderAuthorAdditions = function(team) {
    var authorAdditionsQuery = makeQuery(createMatches(team), [createSegmentGroup("author"),createDurationGroup("minute")], [createReducer("additions","sum")])
    $.getJSON('query?q=' + toEncodedJson(authorAdditionsQuery), function(data) {
        var series = data.results.map(createTeamData("additions-sum"));
        var pieSeries = series.map(createPieData);

        $('#spline-adds-author').highcharts({
            chart: {
                zoomType: 'x',
                type: 'spline'
            },
            title: {
                text: 'Lines of Code Over Time'
            },
            xAxis: {
                type: 'datetime'
            },
            yAxis: {
                title: {
                    text: 'Lines of Code'
                }
            },
            series: series
        });

        $('#pie-adds-author').highcharts({
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false
            },
            title: {
                text: 'Total Lines of Code'
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
                data: pieSeries
            }]
        });
    });
};

var renderAll = function(team) {
    renderAuthorCommits(team);
    renderAuthorAdditions(team);
};

$(function () {
    renderAll();
});