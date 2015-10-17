#!/bin/bash

read -p "Input folder name:" name
echo "Folder name is: $name"

find $name -type f -exec md5sum -b '{}' + | sort -k 1 | uniq -w 32 -d | cut -d* -f 2
