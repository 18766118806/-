app.service("uploadService",function ($http) {
    this.uploadFile = function () {
        var  formdata = new FormData();
        formdata.append("file",file.files[0]);  //file :页面上传文件框的name

        return $http({
            url:"../upload.do",
            method:"post",
            data:formdata, //上传的文件内容
            headers:{"Content-Type":undefined},   //默认是json
            transformRequest:angular.identity   //二进制序列化
        })

    }
})

