#!/bin/bash
PS3='选择功能： '
funs=("后端" "转膜仪" "孵育" "退出")

web() {
    cd ZkTony-WEB/
    chmod +x ./gradlew
    ./gradlew bootBuildImage
    echo -n "输入后端版本:" 
    read ver
    docker tag web:$ver shenmo1234/web:latest
    docker push shenmo1234/web:latest
    docker pull shenmo1234/web:latest
    docker stop web
    docker rm web
    docker run -d --name web -p 8080:8080 -v /zktony:/zktony shenmo1234/web:latest
    echo "打包上传启动完成"
    exit
}

zm() {
    cd ZkTony-ZM/
    chmod +x ./gradlew
    ./gradlew assembleRelease
    echo -n "输入版本:" 
    read ver
    cp app/release/zktony-zm-$ver-release.apk /zktony/
}

fy() {
    cd ZkTony-FY/
    chmod +x ./gradlew
    ./gradlew assembleRelease
    echo -n "输入版本:" 
    read ver
    cp app/release/zktony-fy-$ver-release.apk /zktony/
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