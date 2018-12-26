app.controller("searchController", function ($scope, searchService) {

    //定义搜索对象
    $scope.searchMap = {'keywords': '', 'category': '', 'brand': '', 'spec': {}};

    $scope.search = function () {
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
            }
        )
    }

    //添加条件执行方法
    $scope.addSearchMap = function (key, value) {   //key :点击的的属于什么选项 value : 点击的
        if (key === "category") {
            $scope.searchMap.category = value;
        }
        else if (key === "brand") {
            $scope.searchMap.brand = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
    }

    //移除条件显示
    $scope.removeSearchMap = function (key) {
        if (key === "category") {
            $scope.searchMap.category = "";
        }
        else if (key === "brand") {
            $scope.searchMap.brand = "";
        } else {
            delete $scope.searchMap.spec[key];

        }

    }

});


