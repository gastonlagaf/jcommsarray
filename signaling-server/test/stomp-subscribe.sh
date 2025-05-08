REQUEST="SUBSCRIBE\nid:${1}\ndestination:${2}\n\n\0"

printf "$REQUEST" '-n' | od -A n -t x1 | sed 's/ *//g'