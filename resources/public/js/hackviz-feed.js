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



var addCommit = function(commit) {
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
};

addCommit({additions: 1,
author: "gilbertw1",
"avatar-url": "https://assets.github.com/images/gravatars/gravatar-140.png",
deletions: 0,
msg: "commit message!!!",
owner: "gilbertw1",
repo: "psychic-wallhack",
team: "Super Crew",
time: 1384296419000,
ts: 1384296423117})