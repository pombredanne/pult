all: staticpages

release: clean staticpages css compile rm-target pack

#generates static pages from Markdown
#it expects that you have markdown.js already installed
#if not, run `npm install markdown`
staticpages:
	md2html CHANGELOGS.md > static/changelogs.html
	md2html LICENSE.md > static/license.html
	md2html README.md > static/readme.html

#compile all garden-files into css stylepages
css:
	lein garden once

clean:
	lein clean
	rm -f js/pult*.js
	rm -rf ./target
	rm -rf ./test
	rm -f pult.zip

rm-target:
	rm -rf ./target

compile:
	lein cljsbuild once prod

pack:
	zip -r pult.zip *

