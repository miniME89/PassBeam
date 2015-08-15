#!/bin/sh

keysymdefFilepath="/usr/include/X11/keysymdef.h"

if [ ! -f $keysymdefFilepath ]; then
    echo "file '$keysymdefFilepath' not found"
    exit 1
fi

echo "#<keysym_name>,<keysym_value>,<unicode_value>,<unicode_name>"
cat $keysymdefFilepath | perl -ne 'if (/#define XK_(\w+)\s+(0x[0-9a-fA-F]*)(\s*\/\*\s*U\+([0-9a-fA-F]{4})\s+(.+?)\s*\*\/)?/) { print "$1,$2"; if($3) { print ",0x$4,$5"; } print "\n"; }'
