REQUEST="CONNECT\naccept-version:1.2\nhost:${1}\n\n\0"

printf "$REQUEST" '-n' | od -A n -t x1 | sed 's/ *//g'