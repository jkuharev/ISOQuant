#!/bin/bash
cd "`dirname "$0"`"

function txt2tex2pdf2html()
{
	fileBase=${1}
	# pandoc -r markdown -w html -s --number-sections --toc -o help.html help.txt
	pandoc -r markdown -w latex -s --number-sections --toc --listings --include-in-header=header.tex -o $fileBase.tex $fileBase.txt
	
	pdflatex $fileBase
	pdflatex $fileBase
	htlatex $fileBase "xhtml, -css"
	
	mv $fileBase.html $fileBase.html.tmp
	
	# we also need to remove ids like id\=\"QQ[0-9]+\-[0-9]+\-[0-9]+\"
	# because silverstripe does not like them
	sed -e 's/id="QQ[0-9]*-[0-9]*-[0-9]*"//g' $fileBase.html.tmp > $fileBase.html
	
	# clean up
	rm *.toc *.out *.xref *.tmp *.dvi *.aux *.4tc *.4ct *.lg *.idv *.log
}

txt2tex2pdf2html "how2operate"
txt2tex2pdf2html "how2install"