app.controller('indexController', function ($scope, $controller, loginService) {
    $scope.showLogin = function () {
        loginService.loginName().success(
            function (response) {
                $scope.username = response.username;
            })
    }
});


