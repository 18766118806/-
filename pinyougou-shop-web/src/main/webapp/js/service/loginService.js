//获取当前用户名
app.service('loginService',function($http) {

    this.loginName = function () {
        return $http.get('../login/username.do');
    }
});