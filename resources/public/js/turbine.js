var makeQuery = function(matches, groups, reducers) {
    return {
        match: matches,
        group: groups,
        reduce: reducers
    }
}

var createMatch = function(entity, op, value) {
    var matchOp = {};
    matchOp[op] = value;
    var match = {};
    match[entity] = matchOp;
    return match;
};

var createReducer = function(entity, op) {
    var reduce = {};
    reduce[op] = entity;
    var reducer = {};
    reducer[(entity + "-" + op)] = reduce;
    return reducer;
};

var createDurationGroup = function(duration) {
    return {duration: duration, offset: 5};
};

var createSegmentGroup = function(segment) {
    return {segment: segment};
};

var toEncodedJson = function(obj) {
    return encodeURIComponent(JSON.stringify(obj))
};