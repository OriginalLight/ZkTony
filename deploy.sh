#!/bin/bash
function menu ()
{
cat << EOF
----------------------------------------
|***************  Menu ****************|
----------------------------------------
`echo -e "\033[35m 1)后端\033[0m"`
`echo -e "\033[35m 2)转膜仪\033[0m"`
`echo -e "\033[35m 3)孵育\033[0m"`
`echo -e "\033[35m 4)四通道分液\033[0m"`
`echo -e "\033[35m 5)测试程序\033[0m"`
`echo -e "\033[35m 6)退出\033[0m"`
EOF
read -p "Please select：" input
case $input in
	1)	
		web
	;;
	2)
		zm
	;;
	3)
		fy
	;;
	4)
		liquid
	;;
    5)
		test_program
	;;
	6)
		exit 0
	;;
	*)
		echo "Input Error ,Please again !!!"
		exit 1
	;;
esac
}

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

test_program() {
    echo -n "输入测试程序版本:" 
    read ver
    cd ZkTony-TEST/
    chmod +x ./gradlew
    ./gradlew assembleRelease
    cp app/build/outputs/apk/release/zktony-test-$ver-release.apk /zktony/
    echo "打包更新四通道分液安装包完成"
    exit
}