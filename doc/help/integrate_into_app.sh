#!/bin/bash

cd "`dirname "$0"`"

cp help.css ../../src/isoquant/plugins/help/html/help.css
cp help.css ../../bin/isoquant/plugins/help/html/help.css

cp help.html ../../src/isoquant/plugins/help/html/help.html
cp help.html ../../bin/isoquant/plugins/help/html/help.html

cp pic/*.png ../../src/isoquant/plugins/help/html/pic/
cp pic/*.png ../../bin/isoquant/plugins/help/html/pic/