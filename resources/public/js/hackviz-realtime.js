var wsUri = window.location.href.replace(/^http(s?:\/\/.*)\/.*$/, 'ws$1/event-stream');
var socket = new WebSocket("ws://localhost:8080/happiness");

socket.onmessage = function(event) {
    console.log(JSON.parse(event.data))
}