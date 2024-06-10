#!/bin/bash

# Function to install a font package
install_font() {
    echo "Installing font package: $1"
    if ! apk add "$1" --no-cache; then
        echo "Failed to install $1"
    fi
}

# Map languages to specific font packages
declare -A language_fonts=(
    ["ar_AR"]="font-noto-arabic"
    ["zh_CN"]="font-isas-misc"
    ["zh_TW"]="font-isas-misc"
    ["ja_JP"]="font-noto font-noto-thai font-noto-tibetan font-ipa font-sony-misc font-jis-misc"
    ["ru_RU"]="font-vollkorn font-misc-cyrillic font-mutt-misc font-screen-cyrillic font-winitzki-cyrillic font-cronyx-cyrillic"
    ["sr_LATN_RS"]="font-vollkorn font-misc-cyrillic font-mutt-misc font-screen-cyrillic font-winitzki-cyrillic font-cronyx-cyrillic"
    ["uk_UA"]="font-vollkorn font-misc-cyrillic font-mutt-misc font-screen-cyrillic font-winitzki-cyrillic font-cronyx-cyrillic"
    ["ko_KR"]="font-noto font-noto-thai font-noto-tibetan"
    ["el_GR"]="font-noto"
    ["hi_IN"]="font-noto-devanagari"
    ["bg_BG"]="font-vollkorn font-misc-cyrillic"
    ["GENERAL"]="font-terminus font-dejavu font-noto font-noto-cjk font-awesome font-noto-extra"
)

# Install all fonts from the language_fonts map
for fonts in "${language_fonts[@]}"; do
    for font in $fonts; do
        install_font $font
    done
done
