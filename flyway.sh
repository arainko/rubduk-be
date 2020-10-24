#!/bin/bash
username="postgres"
password="postgres"
url="jdbc:postgresql://localhost:5432/rubduk"
action="flywayMigrate"

while getopts n:p:u:c flag
do
    # shellcheck disable=SC2220
    case "${flag}" in
        n) username=${OPTARG};;
        p) password=${OPTARG};;
        u) url=${OPTARG};;
        c) action="flywayClean";;
    esac
done
echo "Running Flyway with parameters:"
echo "  username: $username";
echo "  password: $password";
echo "  URL: $url";
echo "";
echo "Executing '$action'";
echo "";
echo "Starting SBT...";
sbt -Dflyway.url="$url" -Dflyway.user="$username" -Dflyway.password="$password" "$action";
echo "--- Done ---";