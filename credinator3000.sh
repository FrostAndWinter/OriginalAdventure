#!/bin/bash
# add Contributors to every java file
git ls-tree -r --name-only master ./ | while read file ; do
if [[ "$file" =~ java$ ]] ; then
    echo "=== $file"

    git log --follow --pretty=format:%an -- $file | sort | uniq | while read name ; do
        name=$( echo "$name" | sed -e "s/liamoneill/Liam O'Neill/")
        name=$( echo "$name" | sed -e "s/troughton/Thomas Roughton/")

        name=$( echo "$name" | sed -e "s/David Barnett/David Barnett \(barnetdavi\) \(300313764\)/")
        name=$( echo "$name" | sed -e "s/Liam O'Neill/Liam O'Niell \(oneilliam\) \(300312734\)/")
        name=$( echo "$name" | sed -e "s/Joseph Bennett/Joseph Bennett \(bennetjose\) \(300319773\)/")
        name=$( echo "$name" | sed -e "s/Daniel Braithwaite/Daniel Braithwaite \(braithdani\) \(300313770\)/")
        name=$( echo "$name" | sed -e "s/Thomas Roughton/Thomas Roughton \(roughtthom\) \(300313924\)/")

        echo -n '/*' "$name" '*/' $'\n' "$(cat $file)" > "$file"
        echo $name
    done
    echo -n '/* Contributor List  */' $'\n' "$(cat $file)" > "$file"
fi
done
