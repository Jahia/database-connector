#!/usr/bin/env bash
START_TIME=$SECONDS
MANIFEST=$1

if [[ $MANIFEST == "" ]]; then
  MANIFEST="warmup-manifest-snapshot.yml"
  echo " == No Warmup manifest provided, using: ${MANIFEST}"
fi

echo " == Waiting for Jahia to startup http://${JAHIA_HOST}:${JAHIA_PORT}"
jahia-cli alive --jahiaAdminUrl=http://${JAHIA_HOST}:${JAHIA_PORT}

ELAPSED_TIME=$(($SECONDS - $START_TIME))
echo " == Jahia became alive in ${ELAPSED_TIME} seconds"

# Execute jobs listed in the manifest
sed -i -e "s/NEXUS_USERNAME/${NEXUS_USERNAME}/g" /tmp/buildEnv/${MANIFEST}
sed -i -e "s/NEXUS_PASSWORD/${NEXUS_PASSWORD}/g" /tmp/buildEnv/${MANIFEST}
echo "Download artifacts from Nexus"
echo "/tmp/buildEnv/${MANIFEST}"
jahia-cli manifest:run --manifest=/tmp/buildEnv/${MANIFEST} --jahiaAdminUrl=http://${JAHIA_HOST}:${JAHIA_PORT}
END_BUILD=$(($SECONDS - $INT_TIME))
echo " == Jahia was updated in ${END_BUILD} seconds"

echo "Run cypress tests"
yarn cypress run
echo "Tests Done"