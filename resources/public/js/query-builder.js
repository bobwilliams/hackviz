$(function() {
    $("#go-btn").click(function() {
        renderGraph();
    });
});

var buildQuery = function() {
    var matches = getMatches();
    var groups = getGroups();
    var reducers = getReducers();
    return makeQuery(matches, groups, reducers);
}

var getTimeString = function(timeValue) {
    if(timeValue === "year") {
        return "YYYY";
    } else if(timeValue === "month") {
        return "YYYYMM";
    } else if(timeValue === "day") {
        return "YYYYMMDD";
    } else if(timeValue === "hour") {
        return "YYYYMMDDhh";
    } else if(timeValue === "minute") {
        return "YYYYMMDDhhmm";
    }
}

var selected = function(el) {
    return el.val();
}

var timeValue = function() {
    return selected($("#group-times"));
}

var first = function(obj) {
    for(var key in obj) {
        return obj[key];
    }
}

var createTimeData = function() {
    return function(timeResults) {
        var res;

        res = timeResults.data[0].data[0];

        console.log(timeResults);

        return [
            moment(timeResults.group, getTimeString(timeValue())).unix()*1000,
            first(res)
        ];
    };
};

var createGroupedData = function() {
    return function(results) {
        return {
            name: results.group,
            data: results.data.map(createTimeData())
        };
    };
};

var renderGraph = function() {
    var query = buildQuery();

    $.getJSON('commits?'+query, function(data) {
        var series;

        if(selected($("#group-entities")) === "none") {
            series = [{
                name: "Results", 
                data: data.map(createTimeData())
            }];
        } else {
            series = data.map(createGroupedData());
        }
        
        console.log(series);

        $('#dynamic-graph').highcharts({
            chart: {
                zoomType: 'x',
                type: 'spline'
            },
            title: {
                text: 'Query Results'
            },
            xAxis: {
                type: 'datetime'
            },
            yAxis: {
                title: {
                    text: 'Results'
                }
            },
            series: series
        });
    });
}

var makeQuery = function(matches, groups, reducers) {
    return "groups="+groups+"&metrics="+reducers+"&"+matches;
}

var matchOp = function() {
    var op = selected($("#match-ops"));
    if(op === "=") {
        return "eq";
    } else if(op === ">") {
        return "gt";
    } else if(op === ">=") {
        return "gte";
    } else if(op === "<") {
        return "lt";
    } else if(op === "<=") {
        return "lte";
    } else if(op === "!=") {
        return "ne";
    }
}

var getMatches = function() {
    return selected($("#match-entities"))+"="+matchOp()+":"+$("#match-val").val();
}

var getGroups = function() {
    var entity = selected($("#group-entities"));
    var time = selected($("#group-times"))
    if(entity === "none") {
        return time;
    } else {
        return entity+","+time;
    }
}

var getReducers = function() {
    var reduceEntity = selected($("#reduce-entities"));
    var entity = reduceEntity === "commit" ? "repo" : reduceEntity;

    return entity+":"+selected($("#reduce-ops"));
}