#!/bin/sh

# Get main javascript file
FILE=$(find -type f -name "main*.js")

# Replace API URL placeholder with the passed environment variable
sed -i "s,{API_URL},${API_URL}," $FILE

# Run NGINX server
nginx -g "daemon off;"