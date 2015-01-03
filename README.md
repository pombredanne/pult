# Pult

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/tauho/pult?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Pult ( *i.e "remote controller" in estonian* ) is *HTML5* app that turns any device that can run modern web browser into a game controller. 
It's designed for mobilephones with touchscreens, but it works also on a desktop or on a raspberryPI with keyboard.

![Pult](img/pult.png)

I like to play retrogames and a laptop is not the most comfortable gaming device and believe me 8bit games look much better on 27" screen or projector - pixels are bigger. So i built this app to get away from a laptop, stream a laptop screen onto a bigger screen and play on a couch.

Plus it has longer range than you average USB remote controller, easy to switch keyboard mappings and it makes it possible to games with team.


## Usage

Using the app should be simple:

 * open `index.html` on modern browser. FxOS owner has much more choices: install it with FirefoxIDE or via app-store and open the app.
 
 * check does a correct keymapping in an app configuration is activated
 	* if there's no keymappings yet, then click "+" button on right-top corner, it will show mappings editor.
  	
 * activate your WIFI
 * connect to `pult-server` running on your laptop.

[`Pult-server`](https://github.com/tauho/pult-server) is web-server (written in Clojure) that listen incoming messages from pult-app and turns them into native keyboard actions.

## Contributing

Pult is currently in alpha stage:

 * full of technical debts - will be solved in the next release `0.2`
 * a lag is bigger than it should be - planned for `0.2`
 * design is made by programmer - will be solved in `?`
 * no localization yet
 * documentation is in stage "send me email with your questions";

It's my personal fun project and i made code publicly available so you can also learn more about Clojurescript and maybe fix some bugs, open tickets - it's much more effective than trolling on app-store;

The sourcecode is available on github [https://github.com/tauho/pult](https://github.com/tauho/pult). 

#### Basic contribution workflow

* open issue or send message on Gitter
* Let's discuss about it.
* Forked it.
* Make changes on new branch;
* Dont forget to add tests.
* Make pull request.
