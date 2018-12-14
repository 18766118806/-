//分页查询
//分页查询控件设置
app.controller('baseController', function ($scope) {

    //重新加载
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage)
    };

    $scope.paginationConf = {
        currentPage: 1,

        totalItems: 10,

        itemsPerPage: 10,

        perPageOptions: [10, 20, 30, 40, 50],

        onChange: function () {
            $scope.reloadList();  //调用reloadList重新加载
        }
    };


    $scope.selectIds = [];   //定义集合存储被选中的id
// 复选框点击调用方法
    $scope.updateSelection = function ($event, id) {   //

        if ($event.target.checked) {  //被选中
            $scope.selectIds.push(id)
        } else {
            var idx = $scope.selectIds.indexOf(id); //id在数组中的位置
            // 数组的 splice 方法：从数组的指定位置移除指定个数的元素 ，参数 1 为位置 ，参数2 位移除的个数
            $scope.selectedIds.splice(idx, 1)
        }
    };
});
