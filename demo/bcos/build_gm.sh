#!/bin/bash
set -e
LANG=en_US.UTF-8

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO][FISCO BCOS] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR][FISCO BCOS] ${content}\033[0m"
}

Download() {
    local url=${1}
    local file=$(basename ${url})
    if [ ! -e ${file} ]; then
        curl -#LO ${url}
    fi
}

build_bcos_chain() {
    # Download
    LOG_INFO "Download build_chain.sh ..."
    Download https://github.com/FISCO-BCOS/FISCO-BCOS/raw/master/tools/build_chain.sh
    chmod u+x build_chain.sh

    # Build chain
    LOG_INFO "Build chain ..."
    if [ ! -e ipconf ]; then
        echo "127.0.0.1:4 agency1 1" >ipconf
    fi

    # build chain
    if [ "$(uname)" == "Darwin" ]; then
        # Mac
        if [ -e fisco-bcos-macOS.tar.gz ]; then
            rm -f ./fisco-bcos
            tar -zxvf fisco-bcos-macOS.tar.gz
            ./build_chain.sh -f ipconf -p 30310,20210,8555 -e ./fisco-bcos -g -o nodes_gm
        else
            ./build_chain.sh -f ipconf -p 30310,20210,8555 -g -o nodes_gm
        fi
    else
        # Other
        if [ -e fisco-bcos.tar.gz ]; then
            rm -f ./fisco-bcos
            tar -zxvf fisco-bcos.tar.gz
            ./build_chain.sh -f ipconf -p 30310,20210,8555 -e ./fisco-bcos -g -o nodes_gm
        else
            ./build_chain.sh -f ipconf -p 30310,20210,8555 -g -o nodes_gm
        fi
    fi

    ./nodes_gm/127.0.0.1/start_all.sh
}

build_accounts() {
    LOG_INFO "Download get_gm_account.sh ..."
    Download https://raw.githubusercontent.com/FISCO-BCOS/console/master/tools/get_gm_account.sh
    chmod u+x get_gm_account.sh

    # generate accounts
    mkdir -p accounts
    cd accounts

    bash ../get_gm_account.sh # gm
    mv accounts_gm bcos_gm_user1
    cd -
}

main() {
    build_bcos_chain
    build_accounts
    LOG_INFO "SUCCESS: Build FISCO BCOS Guomi demo finish."
}

main
