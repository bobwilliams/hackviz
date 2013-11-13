$(function(){
    var wsUri = window.location.href.replace(/^http(s?:\/\/.*)\/.*$/, 'ws$1/event-stream');
    console.log("Connecting to websocket at: " + wsUri);
    var socket = new WebSocket(wsUri);

    socket.onmessage = function(event) {
        var commits = JSON.parse(event.data);
        for (i in commits) {
            console.log(commits[(commits.length-1) - i]);
            addCommit(commits[(commits.length-1) - i]);
        }
    };
});



/*var addCommit = function(commit) {
    $('#commits').prepend(
        $('<div>', {class: "row"}).append (
            $('<div>', {class: 'col-lg-2 row-time'}).text(commit.time),
            $('<div>', {class: 'col-lg-2 row-photo'}).append($('<img>', {src: commit['avatar-url']})),
            $('<div>', {class: 'col-lg-2 row-user'}).text(commit.author),
            $('<div>', {class: 'col-lg-2 row-team-name'}).text(commit.team),
            $('<div>', {class: 'col-lg-4 row-commit-msg'}).text(commit.msg)
            //$('<div>', {class: 'col-lg-4 row-commit-additions'}).text(commit.additions),
            //$('<div>', {class: 'col-lg-4 row-commit-deletions'}).text(commit.deletions)
        )
    );
};*/

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
                    $('<div>', {class: "time-text"}).text(moment(commit.time).format('MMMM Do YYYY, h:mm:ss a')),
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

addCommit({additions: 1,
author: "gilbertw1",
"avatar-url": "https://assets.github.com/images/gravatars/gravatar-140.png",
deletions: 0,
msg: "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
owner: "gilbertw1",
repo: "psychic-wallhack",
team: "Super Crew",
time: 1384296419,
ts: 1384296423117})