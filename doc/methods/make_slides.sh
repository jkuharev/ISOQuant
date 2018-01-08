#!/bin/bash

cd "`dirname "$0"`"

fileName="Probleme_der_LFLCMS_Datenanalyse"

params="--from=markdown --mathml --standalone --self-contained --number-sections --columns=80 --toc $fileName.txt"

# make slidy
# pandoc $params --to=slidy --output=slidy.html
# pandoc $params --to=s5 --output=s5.html

# pandoc --to=s5 -s --template=default.impress --output=slides.html slides.txt

# make slide show html
# pandoc $params --to=slidy --output="$fileName.slidy.html"
# pandoc $params --to=s5 --output="$fileName.s5.html"
# pandoc $params --to=dzslides --output="$fileName.dzslids.html"
# pandoc $params --to=beamer --output="$fileName.beamer.pdf"

echo "please uncomment the line for YOUR favorite slide format in this script!!!"
