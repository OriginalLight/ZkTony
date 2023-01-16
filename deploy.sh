#!/bin/bash
PS3='选择功能： '
funs=("后端" "转膜仪" "孵育" "四通道分液" "测试程序" "退出")

web() {
    echo -n "输入后端版本:" 
    read ver
    cd ZkTony-WEB/
    chmod +x ./gradlew
    ./gradlew bootBuildImage
    docker tag web:$ver zktony/web:latest
    docker push zktony/web:latest
    docker pull zktony/web:latest
    docker stop web
    docker rm web
    docker run -d --name web -p 8080:8080 -v /zktony:/zktony zktony/web:latest
    echo "打包上传启动后端完成"
    exit
}

zm() {
    echo -n "输入转膜仪版本:" 
    read ver
    cd ZkTony-ZM/
    chmod +x ./gradlew
    ./gradlew assembleRelease
    cp app/build/outputs/apk/release/zktony-zm-$ver-release.apk /zktony/
    echo "打包更新转膜仪安装包完成"
    exit
}

fy() {
    echo -n "输入孵育版本:" 
    read ver
    cd ZkTony-FY/
    chmod +x ./gradlew
    ./gradlew assembleRelease
    cp app/build/outputs/apk/release/zktony-fy-$ver-release.apk /zktony/
    echo "打包更新转膜仪安装包完成"
    exit
}

liquid() {
    echo -n "输入四通道分液版本:" 
    read ver
    cd ZkTony-LIQUID/
    chmod +x ./gradlew
    ./gradlew assembleRelease
    cp app/build/outputs/apk/release/zktony-liquid-$ver-release.apk /zktony/
    echo "打包更新四通道分液安装包完成"
    exit
}

test() {
    liquid() {
    echo -n "输入测试程序版本:" 
    read ver
    cd ZkTony-TEST/
    chmod +x ./gradlew
    ./gradlew assembleRelease
    cp app/build/outputs/apk/release/zktony-test-$ver-release.apk /zktony/
    echo "打包更新四通道分液安装包完成"
    exit
}
}

select fun in "${funs[@]}"; do
    case $fun in
        "后端")
            web
            ;;
        "转膜仪")
            zm
            ;;
        "孵育")
            fy
            ;;
        "四通道分液")
            liquid
            ;;
        "测试程序")
            test
	    break
            ;;
	"退出")
	    exit
	    ;;
        *) echo "invalid option $REPLY";;
    esac
done