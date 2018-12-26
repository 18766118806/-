var app = angular.module("pinyougou", []);  //不需要分页功能的页面

app.filter("trustHtml",['$sce',function ($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
}]);