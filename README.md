# Prototyper
A simple Spigot plugin that allows developers to prototype plugins through advanced JavaScript files.

## Usage

### Installation
- Download or build the plugin.
- Place it in your server's plugin folder.
- Start the server.
- Add/edit/remove script files in the Prototyper plugin folder.
- Use the `/prototyper reload` command to reload scripts during runtime.
- Have fun prototyping.

### Example File
```js
// A simple JavaScript file using features provided by Prototyper

function load() {
  log("Hello, world!");
}

function unload() {
  log("Bye!");
}
```
