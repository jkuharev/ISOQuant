#!/bin/bash

cd "`dirname "$0"`"

unset LANG

# replace image paths by web paths
sed -e 's/pic\//assets\/isoquant\/pics\//g' help.html > online-help.html

# we need to remove ids like id\=\"QQ[0-9]+\-[0-9]+\-[0-9]+\"
# because silverstripe does not like them
# sed -e 's/id="QQ[0-9]*-[0-9]*-[0-9]*"//g' online-help.html > online-help-silverstripe.html
