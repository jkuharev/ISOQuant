#!/bin/bash

cd "`dirname "$0"`"

# pandoc -r markdown -w html -s --number-sections --toc -o help.html help.txt
pandoc -r markdown -w latex -s --number-sections --toc --listings --include-in-header=header.tex --bibliography=zotero.bib -o help.tex help.txt

pdflatex help
pdflatex help
htlatex help

# replace image paths by web paths
# sed -e 's/pic\//assets\/isoquant\/pics\//g' help.html > online-help.html
# we need to remove ids like id\=\"QQ[0-9]+\-[0-9]+\-[0-9]+\"
# because silverstripe does not like them
# sed -e 's/id="QQ[0-9]*-[0-9]*-[0-9]*"//g' online-help.html > online-help-silverstripe.html

rm *.toc *.out *.xref *.tmp *.dvi *.aux *.4tc *.4ct *.lg *.idv *.log