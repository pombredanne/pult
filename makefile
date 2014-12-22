all: staticpages

#generates static pages from Markdown
#it expects that you have markdown.js already installed
#if not, run `npm install markdown`
staticpages:
	md2html CHANGELOGS.md > static/changelogs.html &&\
	md2html LICENSE.md > static/license.html

