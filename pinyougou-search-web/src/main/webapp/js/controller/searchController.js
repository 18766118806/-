app.controller("searchController", function ($scope, searchService, $location) {

    //定义搜索对象
    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        'spec': {},
        'price': '',
        'pageNo': '1',
        'pageSize': '20',
        'sort': '',
        'sortField': ''
    };

    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        $scope.searchMap.pageSize = parseInt($scope.searchMap.pageSize);
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
                $scope.buildPageLabel();   //构建分页条
            }
        )
    };

    //添加条件执行方法
    $scope.addSearchMap = function (key, value) {   //key :点击的的属于什么选项 value : 点击的
        if (key === "category") {
            $scope.searchMap.category = value;
        }
        else if (key === "brand") {
            $scope.searchMap.brand = value;
        }
        else if (key === "price") {
            $scope.searchMap.price = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
    };

    //构建分页条
    $scope.buildPageLabel = function () {
        $scope.pageLabel = [];
        var firstPage = 1;
        var maxPage = $scope.resultMap.totalPages;
        var lastPage = maxPage;

        $scope.firstDot = true;
        $scope.lastDot = true;
        if (maxPage >= 5) {//总页数大于5
            if ($scope.searchMap.pageNo < 3) {//当前页大于3
                lastPage = 5;
                $scope.firstDot = false;  //前面无点
            } else if ($scope.searchMap.pageNo > lastPage - 2) {  //当前页在最后一页
                firstPage = maxPage - 4;
                $scope.lastDot = false;  // 后面无点
            } else {
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
            for (var index = firstPage; index <= lastPage; index++) {
                $scope.pageLabel.push(index);
            }
        } else {
            //前后都无点
            $scope.firstDot = false;
            $scope.lastDot = false;

        }
    };


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
    //根据页码查询
    $scope.findPage = function (pageNo) {
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }

    //排序查询
    $scope.sort = function (sort, sortField) {
        $scope.searchMap.sort = sort;
        $scope.searchMap.sortField = sortField;
        $scope.search();
    }

    //判断关键字是否在品牌列表
    $scope.keywordsIsBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
                return true;
            }
        }
        return false;
    };

    $scope.loadkeywords = function () {
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }

})
;


