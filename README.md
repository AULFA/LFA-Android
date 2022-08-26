# LFA Reader (Legacy)

The LFA Android Reader application was created to read EPUB books from LFA’s digital collections and is usually shipped as part of our Spark Kits.

## Available Variants
See the Confluence page for a list of available variants of this app: https://aulfa.atlassian.net/wiki/spaces/ACC/pages/29229067/Android+Reader+App+Legacy

## Setup
1. Clone the repo:
```bash
git clone git@github.com:AULFA/LFA-Android.git
```
2. Get the submodules:
```bash
cd LFA-Android
git submodule update --init
```
3. Clone the application-secrets directory into the `.ci` directory, naming it `credentials`:
```bash
cd .ci
git clone git@github.com:AULFA/application-secrets.git credentials
```
4. Run the `credentials.sh` script to set up the required credential files for the app:
```bash
cd ..
.ci-local/credentials.sh
```

Done! You should be able to build the project in any of the available variants now.

## How-tos

### Increment the version number
The version of the LFA Reader app is composed by two parts: version name and version code.

#### Version code
The version code of a specific variant of the app is provided by the `version.properties` file of the variant:
```bash
#
#Thu Aug 25 15:06:13 AEST 2022
VERSION_CODE=1696

```
This file is updated automatically every time you (or Android Studio) rebuild the app, so you shouldn't need to update it manually.
If you need to change it, it's better if you rebuild the app and commit the new content of this file. You can change it manually too, just make sure that the new version code is higher than the previous one.

#### Version name
The version name of the app is defined under the `gradle.properties` file of the project:
```bash
VERSION_NAME=6.1.2-SNAPSHOT
```
You can update this by simply incrementing the version in this variable. Remember that this version name is shared by all the variants of the app.

### Create a new variant
// TODO
