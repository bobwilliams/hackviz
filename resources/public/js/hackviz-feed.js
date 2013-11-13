$(function(){
    var wsUri = window.location.href.replace(/^http(s?:\/\/.*)\/.*$/, 'ws$1/event-stream');
    console.log("Connecting to websocket at: " + wsUri);
    var socket = new WebSocket(wsUri);

    socket.onmessage = function(event) {
        var commits = JSON.parse(event.data);
        for (i in commits) {
            console.log(commits[i]);
            addCommit(commits[i]);
        }
    };
});

var addCommit = function(commit) {
    $('#commits').prepend (
        $('<li>', {class: "list-group-item commit-feed-item new-item"}).append (
            $('<div>', {class: "row"}).append (
                $('<div>', {class: 'col-lg-2 row-photo'}).append (
                    $('<img>', {src: commit['avatar-url'], class: "avatar-img"}),
                    $('<div>', {class: "commit-author"}).text(commit.author),
                    $('<div>', {class: "commit-team"}).text(commit.team)

                ),
                $('<div>', {class: 'col-lg-7 row-commit-msg'}).text(commit.msg),
                $('<div>', {class: 'col-lg-3 row-time-ups-and-downs'}).append (
                    $('<div>', {class: "time-text"}).text(moment(commit.ts).format('MMMM Do YYYY, h:mm:ss a')),
                    $('<div>', {class: "ups-and-downs"}).append (
                        $('<div>', {class: "ups"}).text(commit.additions).prepend (
                            $('<span>', {class: "glyphicon glyphicon-chevron-up ioc-icon"})
                        ),
                        $('<div>', {class: "downs"}).text(commit.deletions).prepend (
                            $('<span>', {class: "glyphicon glyphicon-chevron-down ioc-icon"})
                        )
                    )
                )
            )
        )
    );
};