REQUEST="SEND\ndestination:${1}\n\n$2\0"

printf "$REQUEST" '-n' | od -A n -t x1 | sed 's/ *//g'