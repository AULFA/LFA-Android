#!/bin/bash

#------------------------------------------------------------------------
# A script to copy the necessary credentials into the right places in the
# build.
#

#------------------------------------------------------------------------
# Utility methods
#

fatal()
{
  echo "credentials.sh: fatal: $1" 1>&2
  exit 1
}

info()
{
  echo "credentials.sh: info: $1" 1>&2
}

copy()
{
  FROM="$1"
  TO="$2"
  cp -v "${FROM}" "${TO}" || fatal "could not copy ${FROM} -> ${TO}"
}

copy .ci/credentials/lfa-keystore.jks lfa-keystore.jks

copy .ci/credentials/online-app-credentials.json one.lfa.android.app.bhutan.online/src/main/assets/account_bundled_credentials.json
copy .ci/credentials/online-app-credentials.json one.lfa.android.app.grande/src/main/assets/account_bundled_credentials.json
copy .ci/credentials/online-app-credentials.json one.lfa.android.app.indigenous/src/main/assets/account_bundled_credentials.json
copy .ci/credentials/online-app-credentials.json one.lfa.android.app.laos.online/src/main/assets/account_bundled_credentials.json
copy .ci/credentials/online-app-credentials.json one.lfa.android.app.laos/src/main/assets/account_bundled_credentials.json
copy .ci/credentials/online-app-credentials.json one.lfa.android.app.myanmar.online/src/main/assets/account_bundled_credentials.json
copy .ci/credentials/online-app-credentials.json one.lfa.android.app.online/src/main/assets/account_bundled_credentials.json
copy .ci/credentials/online-app-credentials.json one.lfa.android.app.vietnam.online/src/main/assets/account_bundled_credentials.json

copy .ci/credentials/lfaAnalytics.xml one.lfa.android.app.demo/src/main/assets/lfaAnalytics.xml
copy .ci/credentials/lfaAnalytics.xml one.lfa.android.app.ethiopia/src/main/assets/lfaAnalytics.xml
copy .ci/credentials/lfaAnalytics.xml one.lfa.android.app.fiji/src/main/assets/lfaAnalytics.xml
copy .ci/credentials/lfaAnalytics.xml one.lfa.android.app.globalenglish/src/main/assets/lfaAnalytics.xml
copy .ci/credentials/lfaAnalytics.xml one.lfa.android.app.grande/src/main/assets/lfaAnalytics.xml
copy .ci/credentials/lfaAnalytics.xml one.lfa.android.app.kenya/src/main/assets/lfaAnalytics.xml
copy .ci/credentials/lfaAnalytics.xml one.lfa.android.app.laos/src/main/assets/lfaAnalytics.xml
copy .ci/credentials/lfaAnalytics.xml one.lfa.android.app.myanmar/src/main/assets/lfaAnalytics.xml
copy .ci/credentials/lfaAnalytics.xml one.lfa.android.app.png_offline/src/main/assets/lfaAnalytics.xml
copy .ci/credentials/lfaAnalytics.xml one.lfa.android.app.solomon/src/main/assets/lfaAnalytics.xml
copy .ci/credentials/lfaAnalytics.xml one.lfa.android.app.timor/src/main/assets/lfaAnalytics.xml
copy .ci/credentials/lfaAnalytics.xml one.lfa.android.app.vietnam/src/main/assets/lfaAnalytics.xml

mkdir -p "$HOME/.gradle" ||
  fatal "could not create gradle configuration directory"
cat .ci/credentials/lfa-keystore.properties >> "$HOME/.gradle/gradle.properties" ||
  fatal "could not write credentials"
