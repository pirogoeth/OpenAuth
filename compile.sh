#!/usr/bin/env bash

version=`cat src/plugin.yml | grep version | awk '{print $2}'`

echo "[OpenAuth(${version})] building.]"

javac -Xstdout compile_log.txt -g:none -cp inc/craftbukkit.jar:inc/permissions.jar:inc/bukkit.jar:inc/vault.jar:inc/pex.jar \
    src/me/maiome/openauth/*/*.java \
    src/net/eisental/common/page/*.java src/net/eisental/common/parsing/*.java src/com/sk89q/util/*.java src/com/sk89q/minecraft/util/commands/*.java \
    src/com/sk89q/bukkit/util/*.java


errors=`cat "./compile_log.txt" | tail -n 1`
errors_t=`echo ${errors} | tr -d "[[:space:]]"`

if ! test -z "${errors}" && ! test -z "${errors_t}"; then
    echo "$(cat compile_log.txt)"
    exit 1
fi

jar cvf "OpenAuth-${version}.jar" -C src/ . 2>&1 1>archive_log.txt

cat archive_log.txt

rm ./*_log.txt