#!/bin/bash
PS3='选择功能： '
funs=("后端" "转膜仪" "孵育" "退出")

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
    echo "打包上传启动完成"
    exit
}

zm() {
    echo -n "输入版本:" 
    read ver
    cd ZkTony-ZM/
    chmod +x ./gradlew
    ./gradlew assembleRelease
    cp ZkTony-ZM/app/build/outputs/apk/release/zktony-zm-$ver-release.apk /zktony/
}

fy() {
    echo -n "输入版本:" 
    read ver
    cd ZkTony-FY/
    chmod +x ./gradlew
    ./gradlew assembleRelease
    cp ZkTony-FY/app/build/outputs/apk/release/zktony-fy-$ver-release.apk /zktony/
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
	    break
            ;;
	"退出")
	    exit
	    ;;
        *) echo "invalid option $REPLY";;
    esac
done